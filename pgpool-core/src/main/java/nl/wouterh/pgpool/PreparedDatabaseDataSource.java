package nl.wouterh.pgpool;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * A {@link DataSource} that creates connections for a {@link PreparedDatabase} without any caching
 * or pooling.
 */
public class PreparedDatabaseDataSource implements DataSource {

  private final PreparedDatabase preparedDatabase;

  public PreparedDatabaseDataSource(PreparedDatabase preparedDatabase) {
    this.preparedDatabase = preparedDatabase;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return preparedDatabase.getConnectionProvider().getConnection(preparedDatabase.getName());
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return preparedDatabase.getConnectionProvider()
        .getConnection(preparedDatabase.getName(), username, password);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isInstance(this)) {
      return (T) this;
    }
    throw new SQLException("DataSource of type [" + getClass().getName() +
        "] cannot be unwrapped as [" + iface.getName() + "]");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return iface.isInstance(this);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    throw new UnsupportedOperationException("getLogWriter");
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new UnsupportedOperationException("setLogWriter");
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new UnsupportedOperationException("setLoginTimeout");
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return 0;
  }

  @Override
  public Logger getParentLogger() {
    return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  }
}
