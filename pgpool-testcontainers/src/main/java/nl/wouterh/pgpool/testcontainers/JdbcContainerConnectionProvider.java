package nl.wouterh.pgpool.testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import nl.wouterh.pgpool.ConnectionProvider;
import org.testcontainers.containers.JdbcDatabaseContainer;

/**
 * Implements the {@link ConnectionProvider} for a {@link JdbcDatabaseContainer}. The
 * {@link JdbcDatabaseContainer} may be supplied via a {@link Future} which will be awaited when
 * {@link #getConnection(String)} is called, allowing for asynchronous startup.
 */
public class JdbcContainerConnectionProvider implements ConnectionProvider {

  private final Future<JdbcDatabaseContainer> container;
  private final int originalPort;

  public JdbcContainerConnectionProvider(JdbcDatabaseContainer container, int originalPort) {
    this(CompletableFuture.completedFuture(container), originalPort);
  }

  public JdbcContainerConnectionProvider(Future<JdbcDatabaseContainer> container,
      int originalPort) {
    this.container = container;
    this.originalPort = originalPort;
  }

  protected JdbcDatabaseContainer container() {
    try {
      return container.get();
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  protected String getUsername() {
    return container().getUsername();
  }

  protected String getPassword() {
    return container().getPassword();
  }

  protected String getHost() {
    return container().getHost();
  }

  protected int getPort() {
    return container().getMappedPort(originalPort);
  }

  protected String getDriverScheme() {
    return "postgresql";
  }

  protected String getJdbcUrl(String database) {
    return String.format(
        "jdbc:%s://%s:%d/%s",
        getDriverScheme(),
        getHost(),
        getPort(),
        database
    );
  }

  @Override
  public Connection getConnection(String database) throws SQLException {
    return getConnection(database, getUsername(), getPassword());
  }

  @Override
  public Connection getConnection(String database, String username, String password)
      throws SQLException {
    return DriverManager.getConnection(getJdbcUrl(database), username, password);
  }
}
