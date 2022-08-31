package nl.wouterh.pgpool;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import lombok.Getter;

class DirtyFlaggingDataSource<DS extends DataSource> implements DataSource {

  private final PreparedDatabase preparedDatabase;
  @Getter
  private final DS dataSource;

  public DirtyFlaggingDataSource(PreparedDatabase preparedDatabase, DS dataSource) {
    this.preparedDatabase = preparedDatabase;
    this.dataSource = dataSource;
  }

  @Override
  public Connection getConnection() throws SQLException {
    preparedDatabase.flagDirty();
    return dataSource.getConnection();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    preparedDatabase.flagDirty();
    return dataSource.getConnection(username, password);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return dataSource.getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    dataSource.setLogWriter(out);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return dataSource.getLoginTimeout();
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    dataSource.setLoginTimeout(seconds);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isInstance(this)) {
      return (T) this;
    }
    return dataSource.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return (iface.isInstance(this) || dataSource.isWrapperFor(iface));
  }

  @Override
  public Logger getParentLogger() {
    return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  }
}
