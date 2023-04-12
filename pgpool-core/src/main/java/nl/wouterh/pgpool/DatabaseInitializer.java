package nl.wouterh.pgpool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Initializes a template database by e.g. applying migrations and filling data. Implementations
 * should be deterministic, meaning that if the result of {@link #calculateChecksum} is the same
 * then the effect on the {@link Connection} should also be the same.
 */
public interface DatabaseInitializer {

  byte[] calculateChecksum() throws IOException;

  void run(Connection connection) throws SQLException;

  default void run(ConnectionProvider connectionProvider, String templateName) throws SQLException {
    try (Connection conn = connectionProvider.getConnection(templateName)) {
      run(conn);
    }
  }

}
