package nl.wouterh.pgpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages the creation of template & prepared database for tests
 */
@Slf4j
public class PgPoolManager {

  private final Object startLock = new Object();
  private final Object lock = new Object();

  private final boolean waitForDropOnShutdown;
  private final int dropThreads;
  private final ConnectionProvider connectionProvider;
  private final Duration takeDurationThreshold;
  private final Map<String, PooledDatabase> pooledDatabases;
  private final Map<String, PreparedDatabase> preparedDatabases = new ConcurrentHashMap<>();
  private final Map<String, BlockingQueue<PreparedDatabase>> createQueues = new ConcurrentHashMap<>();
  private final Set<PreparedDatabase> createdDatabases = Collections.newSetFromMap(
      new ConcurrentHashMap<>());
  private final BlockingQueue<PreparedDatabase> dropQueue = new LinkedBlockingQueue<>();
  private final List<Task> createTasks = new ArrayList<>();
  private final List<Task> dropTasks = new ArrayList<>();
  private boolean stopped;
  private boolean started;
  private final boolean shutdownExecutor;
  private final ExecutorService executor;
  private final DatabaseOperations databaseOperations;

  public PgPoolManager(PgPoolConfig config) {
    this.dropThreads = config.getDropThreads();
    this.waitForDropOnShutdown = config.isWaitForDropOnShutdown();
    this.connectionProvider = config.getConnectionProvider();
    this.pooledDatabases = config.getPooledDatabases().stream()
        .collect(Collectors.toMap(PooledDatabase::getName, Function.identity()));
    this.takeDurationThreshold = config.getTakeDurationThreshold();
    this.databaseOperations = config.getDatabaseOperations();

    if (config.getExecutor() != null) {
      executor = config.getExecutor();
      shutdownExecutor = false;
    } else {
      executor = Executors.newCachedThreadPool();
      shutdownExecutor = true;
    }

    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
  }

  /**
   * Schedule {@link #start()} to be called via the configured {@link PgPoolConfig#getExecutor()}
   */
  public void scheduleStart() {
    executor.submit(log(() -> {
      start();
      return null;
    }));
  }

  /**
   * Creates the {@link PgPoolConfig#getPooledDatabases()} and runs their
   * {@link PooledDatabase#getInitializers()}
   *
   * @return false if already starting on another thread
   */
  public void start() throws InterruptedException, SQLException {
    synchronized (startLock) {
      synchronized (lock) {
        if (stopped) {
          throw new IllegalStateException("Can not restart");
        }

        if (started) {
          return;
        }
      }

      Instant startTime = Instant.now();
      log.debug("Starting dropper thread");
      for (int i = 0; i < dropThreads; i++) {
        startDropThread("pgpool-dropper-" + i,
            new DatabaseDropperRunnable(
                databaseOperations,
                dropQueue,
                createdDatabases,
                connectionProvider
            ));
      }

      List<Future<Object>> futures = pooledDatabases.values().stream()
          .map(pooledDatabase -> executor.submit(log(() -> {
            new DatabaseTemplateInitializeRunnable(databaseOperations, connectionProvider,
                pooledDatabase).run();

            BlockingQueue<PreparedDatabase> createQueue;
            if (pooledDatabase.getSpares() <= 0) {
              createQueue = new SynchronousQueue<>();
            } else {
              createQueue = new ArrayBlockingQueue<>(pooledDatabase.getSpares());
            }

            createQueues.put(pooledDatabase.getName(), createQueue);
            for (int i = 0; i < pooledDatabase.getCreateThreads(); i++) {
              startCreateThread("pgpool-creator-" + pooledDatabase.getName() + "-" + i,
                  new DatabaseCreatorRunnable(
                      databaseOperations,
                      createQueue,
                      createdDatabases,
                      pooledDatabase,
                      connectionProvider
                  ));
            }

            return null;
          }))).collect(Collectors.toList());

      for (Future<Object> future : futures) {
        try {
          future.get();
        } catch (ExecutionException e) {
          log.warn("Error initializing pooled database", e);
          stopped = true;

          if (e.getCause() instanceof SQLException) {
            throw (SQLException) e.getCause();
          }
          throw new RuntimeException(e);
        }
      }

      started = true;
      log.info("Took {} to start", Duration.between(startTime, Instant.now()));
    }
  }

  public void beforeEach() throws Exception {
    if (!started) {
      start();
    }

    if (stopped) {
      throw new IllegalStateException("stopped");
    }

    for (PooledDatabase pooledDatabase : pooledDatabases.values()) {
      PreparedDatabase previousDatabase = preparedDatabases.get(pooledDatabase.getName());
      if (previousDatabase == null) {
        log.debug("Creating new database for {}", pooledDatabase.getName());
        Instant startTime = Instant.now();
        preparedDatabases.put(pooledDatabase.getName(),
            createQueues.get(pooledDatabase.getName()).take());

        Duration takeTime = Duration.between(startTime, Instant.now());
        if (takeTime.compareTo(takeDurationThreshold) > 0) {
          log.info("Took {} to take prepared database for {}",
              takeTime,
              pooledDatabase.getTemplateDatabaseName());
        }
      }
    }
  }

  public void afterEach() throws Exception {
    if (stopped) {
      throw new IllegalStateException("stopped");
    }

    Iterator<PreparedDatabase> it = preparedDatabases.values().iterator();
    while (it.hasNext()) {
      PreparedDatabase preparedDatabase = it.next();
      if (preparedDatabase.isDirty()) {
        log.debug("Marking {} for drop", preparedDatabase.getName());

        dropQueue.add(preparedDatabase);

        it.remove();
      }
    }
  }

  private void startCreateThread(String name, DatabaseCreatorRunnable runnable) {
    Thread thread = new Thread(runnable, name);
    thread.start();

    createTasks.add(new Task(thread, runnable));
  }

  private void startDropThread(String name, DatabaseDropperRunnable runnable) {
    Thread thread = new Thread(runnable, name);
    thread.start();

    dropTasks.add(new Task(thread, runnable));
  }

  public PreparedDatabase getPreparedDatabase(String templateName) {
    return preparedDatabases.get(templateName);
  }

  public void stop() {
    synchronized (lock) {
      if (stopped) {
        return;
      }

      stopped = true;
    }

    if (shutdownExecutor) {
      executor.shutdown();
    }

    // stop creating new databases
    for (Task createRunnable : createTasks) {
      createRunnable.stop();
      createRunnable.interrupt();
    }

    try {
      for (Task createRunnable : createTasks) {
        createRunnable.join();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    if (waitForDropOnShutdown) {
      // wait for & drop all the created databases
      try {
        dropCreatedDatabases();
      } catch (Exception e) {
        log.warn("Could not drop databases", e);
      }
    }

    // stop drop threads
    for (Task dropRunnable : dropTasks) {
      dropRunnable.stop();
      dropRunnable.interrupt();
    }

    try {
      for (Task dropRunnable : dropTasks) {
        dropRunnable.join();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    createTasks.clear();
    dropTasks.clear();
  }

  private void dropCreatedDatabases() throws Exception {
    if (createdDatabases.isEmpty()) {
      // everything already dropped
      return;
    }

    DatabaseDropperRunnable dropper = new DatabaseDropperRunnable(
        databaseOperations,
        dropQueue,
        createdDatabases,
        connectionProvider
    );

    // get a connection and participate in the dropping process
    try (Connection connection = connectionProvider.getConnection("postgres")) {
      while (!createdDatabases.isEmpty()) {
        while (!dropQueue.isEmpty()) {
          PreparedDatabase next = dropQueue.poll();
          if (next != null) {
            dropper.run(connection, next);
          }
        }

        // in case any created databases were somehow not dropped yet
        dropQueue.addAll(createdDatabases);
      }
    }
  }

  static <T> Callable<T> log(Callable<T> callable) {
    return () -> {
      try {
        return callable.call();
      } catch (Exception e) {
        log.error("Error during scheduler execution", e);
        throw e;
      }
    };
  }
}
