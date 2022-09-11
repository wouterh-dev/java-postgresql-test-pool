package nl.wouterh.pgpool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresOperations implements DatabaseOperations {

  @Override
  public boolean databaseExists(
      Connection conn,
      String name
  ) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(
        "SELECT datname FROM pg_catalog.pg_database WHERE datname = ?")) {
      stmt.setString(1, name);
      ResultSet rs = stmt.executeQuery();
      return rs.next();
    }
  }

  @Override
  public void disableConnections(
      Connection conn,
      String databaseName
  ) throws SQLException {
    try (PreparedStatement statement = conn.prepareStatement(
        "UPDATE pg_database SET datallowconn = FALSE WHERE datname = ?; "
            + "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = ?"
    )) {
      statement.setString(1, databaseName);
      statement.setString(2, databaseName);
      statement.execute();
    }
  }

  @Override
  public void renameDatabase(
      Connection conn,
      String oldName,
      String newName
  ) throws SQLException {
    try (Statement statement = conn.createStatement()) {
      statement.execute(String.format(
          "ALTER DATABASE \"%s\" RENAME TO \"%s\"",
          oldName,
          newName
      ));
    } catch (SQLException e) {
      if (!e.getSQLState().equals("42P04")) {
        // suppress duplicate database name error, which happens if another process is creating the same template database at the same time
        throw e;
      }
    }
  }

  @Override
  public void createDatabase(
      Connection conn,
      String name
  ) throws SQLException {
    try (Statement statement = conn.createStatement()) {
      statement.execute("CREATE DATABASE \"" + name + "\"");
    }
  }

  @Override
  public void createDatabaseFromTemplate(
      Connection conn,
      String name,
      String templateName
  ) throws SQLException {
    try (Statement statement = conn.createStatement()) {
      statement.execute(String.format(
          "CREATE DATABASE \"%s\" TEMPLATE \"%s\"",
          name,
          templateName
      ));
    }
  }

  @Override
  public void dropDatabase(
      Connection conn,
      String name
  ) throws SQLException {
    try (PreparedStatement statement = conn.prepareStatement(
        "UPDATE pg_database SET datallowconn = FALSE WHERE datname = ?; "
            + "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = ?; "
            + "DROP DATABASE \"" + name + "\"")) {
      statement.setString(1, name);
      statement.setString(2, name);
      statement.execute();
    } catch (SQLException e) {
      if (!e.getSQLState().equals("3D000")) {
        // suppress database does not exist errors
        throw e;
      }
    }
  }
}
