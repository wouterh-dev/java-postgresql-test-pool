package nl.wouterh.pgpool;

import static nl.wouterh.pgpool.PooledDatabase.PREPARED_PREFIX;
import static nl.wouterh.pgpool.PooledDatabase.TEMPLATE_PREFIX;
import static nl.wouterh.pgpool.PooledDatabase.WIP_PREFIX;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider {

  /**
   * Get a connection to database using the default username & password
   *
   * @param database name of the database
   * @return the connection
   * @throws SQLException if connection could not be established
   */
  Connection getConnection(String database) throws SQLException;

  /**
   * Get a connection to database using a specified username & password
   *
   * @param database name of the database
   * @param username the username to use
   * @param password the password to use
   * @return the connection
   * @throws SQLException if connection could not be established
   */
  Connection getConnection(String database, String username, String password) throws SQLException;

  static boolean isDatabaseForTemplate(String database, String template) {
    return database.startsWith(WIP_PREFIX + template + "_")
        || database.startsWith(TEMPLATE_PREFIX + template + "_")
        || database.startsWith(PREPARED_PREFIX + template + "_");
  }

  static boolean isTemplateDatabase(String database) {
    return database.startsWith(WIP_PREFIX)
        || database.startsWith(TEMPLATE_PREFIX);
  }

  static boolean isPreparedDatabase(String database) {
    return database.startsWith(PREPARED_PREFIX);
  }
}
