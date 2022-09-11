package nl.wouterh.pgpool;

import static nl.wouterh.pgpool.PooledDatabase.WIP_PREFIX;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class DatabaseTemplateInitializeRunnable {

  private final byte[] buf = new byte[20];

  private final DatabaseOperations databaseOperations;
  private final ConnectionProvider connectionProvider;
  private final PooledDatabase pooledDatabase;

  public void run() throws SQLException, InterruptedException {
    try (Connection controlConnection = connectionProvider.getConnection("postgres")) {
      // check if database already exists
      if (databaseOperations.databaseExists(controlConnection,
          pooledDatabase.getTemplateDatabaseName())) {
        // The template database has already been created
        log.debug("Template database {} already present",
            pooledDatabase.getTemplateDatabaseName());
        return;
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

      databaseOperations.createDatabase(controlConnection, workName);

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
      databaseOperations.renameDatabase(controlConnection, workName,
          pooledDatabase.getTemplateDatabaseName());

      // and prevent any connections
      databaseOperations.disableConnections(controlConnection,
          pooledDatabase.getTemplateDatabaseName());
    }
  }
}
