package nl.wouterh.pgpool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class DatabaseDropperRunnable extends ConnectionRunnable {

  private final BlockingQueue<PreparedDatabase> queue;
  private final Set<PreparedDatabase> createdDatabases;

  public DatabaseDropperRunnable(
      BlockingQueue<PreparedDatabase> queue,
      Set<PreparedDatabase> createdDatabases,
      ConnectionProvider connectionProvider
  ) {
    super(connectionProvider);
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

      drop(connection, next);
    } catch (SQLException e) {
      if (!e.getSQLState().equals("3D000")) {
        // suppress database does not exist errors
        throw e;
      }
    } finally {
      createdDatabases.remove(next);
    }
  }

  protected void drop(Connection connection, PreparedDatabase next) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(
        "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = ?; "
            + "DROP DATABASE \"" + next.getName() + "\"")) {
      statement.setString(1, next.getName());
      statement.execute();
    } catch (SQLException e) {
      if (!e.getSQLState().equals("3D000")) {
        // suppress database does not exist errors
        throw e;
      }
    }
  }
}
