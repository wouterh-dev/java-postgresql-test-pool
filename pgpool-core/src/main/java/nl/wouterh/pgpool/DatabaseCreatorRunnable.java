package nl.wouterh.pgpool;

import static nl.wouterh.pgpool.PooledDatabase.PREPARED_PREFIX;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * Creates databases from a template using {@code CREATE DATABASE dbname TEMPLATE tmplname} queries
 * until the queue is full, then blocks
 */
@Slf4j
class DatabaseCreatorRunnable extends ConnectionRunnable {

  private final byte[] buf = new byte[20];

  private final Set<PreparedDatabase> createdDatabases;
  private final PooledDatabase pooledDatabase;
  private final BlockingQueue<PreparedDatabase> queue;

  public DatabaseCreatorRunnable(
      BlockingQueue<PreparedDatabase> queue,
      Set<PreparedDatabase> createdDatabases,
      PooledDatabase pooledDatabase,
      ConnectionProvider connectionProvider
  ) {
    super(connectionProvider);
    this.queue = queue;
    this.createdDatabases = createdDatabases;
    this.pooledDatabase = pooledDatabase;
  }

  public void run(Connection connection) throws Exception {
    PreparedDatabase newDatabase = new PreparedDatabase(
        getNextPreparedDatabaseName(),
        pooledDatabase,
        connectionProvider
    );

    try (Statement statement = connection.createStatement()) {
      log.debug("Creating {} template {}", newDatabase.getName(),
          pooledDatabase.getTemplateDatabaseName());

      createDatabase(newDatabase, statement);

      if(pooledDatabase.getListeners() != null) {
        for (PreparedDatabaseLifecycleListener listener : pooledDatabase.getListeners()) {
          listener.afterCreate(newDatabase);
        }
      }

      queue.put(newDatabase);
    } finally {
      createdDatabases.add(newDatabase);
    }
  }

  private void createDatabase(PreparedDatabase newDatabase, Statement statement)
      throws SQLException {
    statement.execute(String.format(
        "CREATE DATABASE \"%s\" TEMPLATE \"%s\"",
        newDatabase.getName(),
        pooledDatabase.getTemplateDatabaseName()
    ));
  }

  private String getNextPreparedDatabaseName() throws NoSuchAlgorithmException {
    MessageDigest sha1 = MessageDigest.getInstance("SHA1");
    sha1.update(pooledDatabase.getTemplateDatabaseName().getBytes(StandardCharsets.UTF_8));
    ThreadLocalRandom.current().nextBytes(buf);
    sha1.update(buf);
    String hash = Hex.toHexString(sha1.digest()).substring(0, 20);
    return PREPARED_PREFIX + pooledDatabase.getName() + "_" + hash;
  }
}
