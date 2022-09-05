package nl.wouterh.pgpool.spring;

import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.jdbc.datasource.AbstractDataSource;

class ClosedDataSource extends AbstractDataSource {

  @Override
  public Connection getConnection() throws SQLException {
    throw new SQLException("PgPool not yet started");
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    throw new SQLException("PgPool not yet started");
  }
}
