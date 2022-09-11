package nl.wouterh.pgpool;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseOperations {

  /**
   * Checks if the database exists
   *
   * @return true if the database exists
   */
  boolean databaseExists(
      Connection conn,
      String name
  ) throws SQLException;

  /**
   * Prevent future & drop all current connections to a database
   */
  void disableConnections(
      Connection conn,
      String databaseName
  ) throws SQLException;

  /**
   * Rename database oldName to newName
   */
  void renameDatabase(
      Connection conn,
      String oldName,
      String newName
  ) throws SQLException;

  /**
   * Create database
   */
  void createDatabase(
      Connection conn,
      String name
  ) throws SQLException;

  /**
   * Create a database from a template database
   */
  void createDatabaseFromTemplate(
      Connection conn,
      String name,
      String templateName
  ) throws SQLException;

  /**
   * Drop a database
   */
  void dropDatabase(
      Connection conn,
      String name
  ) throws SQLException;
}
