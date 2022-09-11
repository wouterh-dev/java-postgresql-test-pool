package nl.wouterh.pgpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class DatabaseDropperRunnable extends ConnectionRunnable {

  private final DatabaseOperations databaseOperations;
  private final BlockingQueue<PreparedDatabase> queue;
  private final Set<PreparedDatabase> createdDatabases;

  public DatabaseDropperRunnable(
      DatabaseOperations databaseOperations,
      BlockingQueue<PreparedDatabase> queue,
      Set<PreparedDatabase> createdDatabases,
      ConnectionProvider connectionProvider
  ) {
    super(connectionProvider);
    this.databaseOperations = databaseOperations;
    this.queue = queue;
    this.createdDatabases = createdDatabases;
  }

  public void run(Connection connection) throws Exception {
    PreparedDatabase next = queue.take();
    run(connection, next);
  }

  public void run(Connection connection, PreparedDatabase next) throws Exception {
    log.debug("Dropping {}", next.getName());

    try {
      if (next.getPooledDatabase().getListeners() != null) {
        List<PreparedDatabaseLifecycleListener> listeners = next.getPooledDatabase().getListeners();
        for (int i = listeners.size() - 1; i >= 0; i--) {
          PreparedDatabaseLifecycleListener listener = listeners.get(i);
          listener.beforeDrop(next);
        }
      }

      databaseOperations.dropDatabase(connection, next.getName());
    } catch (SQLException e) {
      if (!e.getSQLState().equals("3D000")) {
        // suppress database does not exist errors
        throw e;
      }
    } finally {
      createdDatabases.remove(next);
    }
  }
}
