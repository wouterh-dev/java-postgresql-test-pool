package nl.wouterh.pgpool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

@Data
@Builder
public class PooledDatabase {

  public static final String PREPARED_PREFIX = "db_";
  public static final String WIP_PREFIX = "wip_";
  public static final String TEMPLATE_PREFIX = "tpl_";

  /**
   * Name of the database, must be unique. Will be used as part of the template & prepared database
   * names, and is used to reference this {@link PooledDatabase}
   */
  @NonNull
  private final String name;

  /**
   * Hooks into the {@link PreparedDatabase} lifecycle to run additional tasks, such as managing a
   * connection pool.
   *
   * @see PreparedDatabaseLifecycleListener
   * @see PreparedDatabaseDataSourceFactory
   */
  @Singular
  private final List<PreparedDatabaseLifecycleListener> listeners;

  /**
   * Initialize the template database by e.g. running schema migrations or loading data. See the
   * pgpool-liquibase module for an example.
   *
   * @see DatabaseInitializer
   */
  @Singular
  private final List<DatabaseInitializer> initializers;

  /**
   * Amount of threads used to create new databases. Each thread will create a database, then offer
   * it to the {@link PreparedDatabase} queue, which has a size of {@link #spares}. So there will
   * always be a ({@link #createThreads} + {@link #spares}) amount of prepared databases.
   */
  @Builder.Default
  private int createThreads = 1;

  /**
   * Size of the {@link PreparedDatabase} queue. Useful when there is a large variance in test
   * duration, where some tests take longer to run than others, so during execution of a slower test
   * the queue can be refilled to accommodate for a later burst.
   */
  @Builder.Default
  private int spares = 2;

  @Getter(lazy = true)
  private final String checksum = calculateChecksum();

  private String calculateChecksum() {
    try {
      MessageDigest sha1 = MessageDigest.getInstance("SHA1");

      sha1.update(name.getBytes(StandardCharsets.UTF_8));
      sha1.update((byte) 0);

      if (initializers != null) {
        for (DatabaseInitializer initializer : initializers) {
          sha1.update(initializer.calculateChecksum());
          sha1.update((byte) 0);
        }
      }

      return Hex.toHexString(sha1.digest()).substring(0, 20);
    } catch (NoSuchAlgorithmException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String getTemplateDatabaseName() {
    return TEMPLATE_PREFIX + name + "_" + getChecksum();
  }
}
