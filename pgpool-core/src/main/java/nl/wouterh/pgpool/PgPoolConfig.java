package nl.wouterh.pgpool;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * Configuration of {@link PgPoolManager}
 */
@Data
@Builder
public class PgPoolConfig {

  /**
   * Creates connections to the database server
   */
  private final ConnectionProvider connectionProvider;

  /**
   * Suppresses logging when take duration is shorter than this threshold
   */
  @Builder.Default
  private final Duration takeDurationThreshold = Duration.ofMillis(-1);

  /**
   * Amount of threads used to drop used databases
   */
  @Builder.Default
  private final int dropThreads = 2;

  /**
   * Whether to wait for and drop all prepared databases on shutdown. Template databases will be
   * left behind. Typically, should be true if the database server is used between test runs, and
   * false if the database server is ephemeral.
   */
  @Builder.Default
  private final boolean waitForDropOnShutdown = true;

  /**
   * The databases to create
   *
   * @see PooledDatabase
   */
  @Singular
  private final List<PooledDatabase> pooledDatabases;

  /**
   * ExecutorService to use for launching tasks such as running the {@link DatabaseInitializer}.
   * When null defaults to a {@link Executors#newCachedThreadPool()} which is shutdown when the
   * {@link PgPoolManager} is stopped.
   */
  private final ExecutorService executor;

  @Builder.Default
  private final DatabaseOperations databaseOperations = new PostgresOperations();
}
