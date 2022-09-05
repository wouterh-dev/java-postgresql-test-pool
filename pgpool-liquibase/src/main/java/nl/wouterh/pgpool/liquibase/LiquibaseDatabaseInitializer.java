package nl.wouterh.pgpool.liquibase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import nl.wouterh.pgpool.DatabaseInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link DatabaseInitializer} which integrates {@link Liquibase}
 */
public class LiquibaseDatabaseInitializer implements DatabaseInitializer {

  /**
   * Liquibase has race conditions during changelog parsing:
   * <a href="https://github.com/liquibase/liquibase/issues/2966">LIQUIBASE-2966</a>.
   */
  private static final Object liquibaseRaceConditionLock = new Object();
  private static final Logger log = LoggerFactory.getLogger(LiquibaseDatabaseInitializer.class);

  private final String changeLogFile;
  private final ResourceAccessor resourceAccessor;
  private final String liquibaseContexts;

  public LiquibaseDatabaseInitializer(String changeLogFile) {
    this(changeLogFile, "");
  }

  public LiquibaseDatabaseInitializer(String changeLogFile, String liquibaseContexts) {
    this(changeLogFile, new ClassLoaderResourceAccessor(), liquibaseContexts);
  }

  public LiquibaseDatabaseInitializer(String changeLogFile, ResourceAccessor resourceAccessor,
      String liquibaseContexts) {
    this.changeLogFile = changeLogFile;
    this.resourceAccessor = resourceAccessor;
    this.liquibaseContexts = liquibaseContexts;
  }

  @Override
  public void run(Connection conn) throws SQLException {
    try {
      boolean isAutoCommit = conn.getAutoCommit();
      Contexts contexts = new Contexts(Arrays.stream(liquibaseContexts.split("\\s*,\\s*"))
          .map(String::trim)
          .filter((e) -> !e.isEmpty())
          .toArray(String[]::new));

      synchronized (liquibaseRaceConditionLock) {
        log.info("Starting database migration with contexts {}",
            contexts.getContexts().stream().sorted().collect(Collectors.joining(", ")));
        Database database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(new JdbcConnection(conn));

        Liquibase liquibase = new Liquibase(changeLogFile, resourceAccessor, database);
        liquibase.update(contexts, new LabelExpression());
      }

      conn.commit();
      log.info("Finished migration");

      if (conn.getAutoCommit() != isAutoCommit) {
        conn.setAutoCommit(isAutoCommit);
      }
    } catch (LiquibaseException e) {
      throw new SQLException(e);
    }
  }

  @Override
  public byte[] calculateChecksum() throws IOException {
    try {
      MessageDigest crypt = MessageDigest.getInstance("SHA-1");

      crypt.update(("context=" + liquibaseContexts).getBytes(StandardCharsets.UTF_8));

      String extra = getExtraVersion();
      if (extra != null) {
        crypt.update(("extra=" + extra).getBytes(StandardCharsets.UTF_8));
      }

      List<ChangeSet> changeSets;
      synchronized (liquibaseRaceConditionLock) {
        Liquibase liquibase = new Liquibase(changeLogFile, resourceAccessor, (Database) null);
        changeSets = liquibase.getDatabaseChangeLog().getChangeSets();
      }

      crypt.update(("count=" + changeSets.size()).getBytes(StandardCharsets.UTF_8));

      for (ChangeSet changeSet : changeSets) {
        crypt.update(("author=" + changeSet.getAuthor()).getBytes(StandardCharsets.UTF_8));
        crypt.update(("id=" + changeSet.getId()).getBytes(StandardCharsets.UTF_8));
        crypt.update(("file=" + changeSet.getFilePath()).getBytes(StandardCharsets.UTF_8));
        crypt.update(("checksum=" + changeSet.generateCheckSum()).getBytes(StandardCharsets.UTF_8));
      }

      return crypt.digest();
    } catch (LiquibaseException e) {
      throw new IOException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  protected String getExtraVersion() {
    return null;
  }
}
