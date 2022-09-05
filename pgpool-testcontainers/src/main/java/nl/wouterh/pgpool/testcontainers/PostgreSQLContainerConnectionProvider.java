package nl.wouterh.pgpool.testcontainers;

import static org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT;

import java.util.concurrent.Future;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * A {@link JdbcContainerConnectionProvider} intended to be used with {@link PostgreSQLContainer}
 *
 * @see JdbcContainerConnectionProvider
 * @see PostgreSQLContainer
 */
public class PostgreSQLContainerConnectionProvider extends JdbcContainerConnectionProvider {

  public PostgreSQLContainerConnectionProvider(JdbcDatabaseContainer container) {
    super(container, POSTGRESQL_PORT);
  }

  public PostgreSQLContainerConnectionProvider(Future<JdbcDatabaseContainer> container) {
    super(container, POSTGRESQL_PORT);
  }
}
