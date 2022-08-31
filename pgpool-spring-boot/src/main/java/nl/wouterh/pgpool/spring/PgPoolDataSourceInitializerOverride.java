package nl.wouterh.pgpool.spring;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import nl.wouterh.pgpool.DatabaseInitializer;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Calls {@link PgPoolDataSource#setThreadLocalOverride} before/after the
 * {@link DatabaseInitializer#run(Connection)} to let the database initialization thread access the
 * template database via Spring wired datasource consumers
 */
public class PgPoolDataSourceInitializerOverride implements DatabaseInitializer {

  private final PgPoolDataSource dataSource;
  private final DatabaseInitializer delegate;

  public PgPoolDataSourceInitializerOverride(DataSource dataSource, DatabaseInitializer delegate)
      throws SQLException {
    this.dataSource = dataSource.unwrap(PgPoolDataSource.class);
    this.delegate = delegate;
  }

  @Override
  public byte[] calculateChecksum() throws IOException {
    return delegate.calculateChecksum();
  }

  @Override
  public void run(Connection connection) throws SQLException {
    try {
      dataSource.setThreadLocalOverride(new SingleConnectionDataSource(connection, true));
      delegate.run(connection);
    } finally {
      dataSource.setThreadLocalOverride(null);
    }
  }
}
