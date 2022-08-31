package nl.wouterh.pgpool;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public interface PgPoolManagerAdapter {

  PgPoolManager getManager();

  default Connection createConnection(String templateName) throws SQLException {
    return getDataSource(templateName).getConnection();
  }

  default DataSource getDataSource(String templateName) {
    return getManager().getPreparedDatabase(templateName).getDataSource();
  }
}
