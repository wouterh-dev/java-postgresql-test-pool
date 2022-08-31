package nl.wouterh.pgpool.testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import nl.wouterh.pgpool.ConnectionProvider;
import org.testcontainers.containers.JdbcDatabaseContainer;

public class JcbcContainerConnectionProvider implements ConnectionProvider {

  private final Future<JdbcDatabaseContainer> container;
  private final int originalPort;

  public JcbcContainerConnectionProvider(JdbcDatabaseContainer container, int originalPort) {
    this(CompletableFuture.completedFuture(container), originalPort);
  }

  public JcbcContainerConnectionProvider(Future<JdbcDatabaseContainer> container,
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

  public String getUsername() {
    return container().getUsername();
  }

  public String getPassword() {
    return container().getPassword();
  }

  public String getHost() {
    return container().getHost();
  }

  public int getPort() {
    return container().getMappedPort(originalPort);
  }

  public String getDriverScheme() {
    return "postgresql";
  }

  public String getJdbcUrl(String database) {
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
