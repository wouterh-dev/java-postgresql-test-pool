package nl.wouterh.pgpool;

import static nl.wouterh.pgpool.PooledDatabase.PREPARED_PREFIX;
import static nl.wouterh.pgpool.PooledDatabase.TEMPLATE_PREFIX;
import static nl.wouterh.pgpool.PooledDatabase.WIP_PREFIX;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Similar to a DataSource but creates connections to a named database, and should never return
 * pooled connections
 */
public interface ConnectionProvider {

  /**
   * Get a connection to database using the default username & password. Should open a new
   * connection each time, without any pooling.
   *
   * @param database name of the database
   * @return the newly created connection
   * @throws SQLException if connection could not be established
   */
  Connection getConnection(String database) throws SQLException;

  /**
   * Get a connection to database using a specified username & password. Should open a new
   * connection each time, without any pooling.
   *
   * @param database name of the database
   * @param username the username to use
   * @param password the password to use
   * @return the newly created connection
   * @throws SQLException if connection could not be established
   */
  Connection getConnection(String database, String username, String password) throws SQLException;

  /**
   * Checks if {@code database} is a database created for {@code template}
   *
   * @param database the created database name
   * @param template the template database name
   * @return the result
   */
  static boolean isDatabaseForTemplate(String database, String template) {
    return database.startsWith(WIP_PREFIX + template + "_")
        || database.startsWith(TEMPLATE_PREFIX + template + "_")
        || database.startsWith(PREPARED_PREFIX + template + "_");
  }

  /**
   * Checks if {@code database} is a template database
   *
   * @param database the created database name
   * @return the result
   */
  static boolean isTemplateDatabase(String database) {
    return database.startsWith(WIP_PREFIX)
        || database.startsWith(TEMPLATE_PREFIX);
  }

  /**
   * Checks if {@code database} is a prepared database
   *
   * @param database the created database name
   * @return the result
   */
  static boolean isPreparedDatabase(String database) {
    return database.startsWith(PREPARED_PREFIX);
  }
}
