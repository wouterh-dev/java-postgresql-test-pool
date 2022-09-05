package nl.wouterh.pgpool;

import static nl.wouterh.pgpool.PooledDatabase.WIP_PREFIX;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class DatabaseTemplateInitializeRunnable {

  private final byte[] buf = new byte[20];

  private final ConnectionProvider connectionProvider;
  private final PooledDatabase pooledDatabase;

  public void run() throws SQLException, InterruptedException {
    try (Connection controlConnection = connectionProvider.getConnection("postgres")) {
      // check if database already exists
      try (PreparedStatement stmt = controlConnection.prepareStatement(
          "SELECT datname FROM pg_catalog.pg_database WHERE datname = ?")) {
        stmt.setString(1, pooledDatabase.getTemplateDatabaseName());
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          // The template database has already been created
          log.debug("Template database {} already present",
              pooledDatabase.getTemplateDatabaseName());
          return;
        }
      }

      // create a temporary database to try to initialize with a random name
      MessageDigest sha1;
      try {
        sha1 = MessageDigest.getInstance("SHA1");
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }

      sha1.update(pooledDatabase.getTemplateDatabaseName().getBytes(StandardCharsets.UTF_8));

      ThreadLocalRandom.current().nextBytes(buf);
      sha1.update(buf);

      String hash = Hex.toHexString(sha1.digest()).substring(0, 20);
      String workName = WIP_PREFIX + pooledDatabase.getName() + "_" + hash;

      try (Statement statement = controlConnection.createStatement()) {
        statement.execute("CREATE DATABASE \"" + workName + "\"");
      }

      // initialize the temporary database
      List<DatabaseInitializer> initializers = pooledDatabase.getInitializers();
      if (initializers != null) {
        try (Connection workConnection = connectionProvider.getConnection(workName)) {
          for (DatabaseInitializer initializer : initializers) {
            initializer.run(workConnection);
          }
        }
      }

      // and rename it to its proper name
      try (Statement statement = controlConnection.createStatement()) {
        statement.execute(String.format(
            "ALTER DATABASE \"%s\" RENAME TO \"%s\"",
            workName,
            pooledDatabase.getTemplateDatabaseName()
        ));
      } catch (SQLException e) {
        log.warn("Failed renaming, other process may have created template database already", e);
      }
    }
  }
}
