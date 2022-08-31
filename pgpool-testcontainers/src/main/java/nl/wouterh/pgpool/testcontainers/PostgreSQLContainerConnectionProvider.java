package nl.wouterh.pgpool.testcontainers;

import static org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT;

import java.util.concurrent.Future;
import org.testcontainers.containers.JdbcDatabaseContainer;

public class PostgreSQLContainerConnectionProvider extends JcbcContainerConnectionProvider {

  public PostgreSQLContainerConnectionProvider(JdbcDatabaseContainer container) {
    super(container, POSTGRESQL_PORT);
  }

  public PostgreSQLContainerConnectionProvider(Future<JdbcDatabaseContainer> container) {
    super(container, POSTGRESQL_PORT);
  }
}
