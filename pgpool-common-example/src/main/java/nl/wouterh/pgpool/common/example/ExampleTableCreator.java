package nl.wouterh.pgpool.common.example;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import nl.wouterh.pgpool.DatabaseInitializer;

/**
 * A simple {@link DatabaseInitializer} example which creates a table
 */
public class ExampleTableCreator implements DatabaseInitializer {

  @Override
  public byte[] calculateChecksum() {
    // Checksum is hardcoded & must be manually changed whenever the implementation of run changes
    // if the run method loaded files, then those files could be checksummed instead
    return (ExampleTableCreator.class.getName() + "v1").getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public void run(Connection connection) throws SQLException {
    try (Statement stmt = connection.createStatement()) {
      stmt.execute("CREATE TABLE test_table(test_id int primary key, test_column varchar)");
    }
  }
}
