package nl.wouterh.pgpool.common.example;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nl.wouterh.pgpool.DatabaseInitializer;

public class ExampleTableFiller implements DatabaseInitializer {

  @Override
  public byte[] calculateChecksum() {
    return (ExampleTableFiller.class.getName() + "v3").getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public void run(Connection connection) throws SQLException {
    try (PreparedStatement prep = connection.prepareStatement(
        "INSERT INTO test_table(test_id, test_column) VALUES (?, ?)")) {
      for (int j = 0; j < 100; j++) {
        prep.setObject(1, j);
        prep.setString(2, "Example " + j);
        prep.execute();
      }
    }
  }
}
