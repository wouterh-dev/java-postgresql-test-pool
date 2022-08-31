package nl.wouterh.pgpool;

import java.sql.Connection;
import java.sql.SQLException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class ConnectionRunnable implements StoppableRunnable {

  protected final ConnectionProvider connectionProvider;

  @Getter
  private boolean running;

  protected ConnectionRunnable(ConnectionProvider connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  public void stop() {
    running = false;
  }

  protected abstract void run(Connection connection) throws Exception;

  public final void run() {
    Connection connection = null;
    try {
      running = true;
      int failures = 0;
      while (running) {
        try {
          if (connection == null || connection.isClosed()) {
            connection = connectionProvider.getConnection("postgres");
          }

          run(connection);

          failures = 0;
        } catch (InterruptedException e) {
          throw e;
        } catch (Exception e) {
          log.warn("Handled exception", e);
          Thread.sleep((long) (1 * (Math.pow(2, Math.min(10, failures)))));
          failures++;
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      running = false;
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          log.warn("Could not close connection", e);
        }
      }
    }
  }
}
