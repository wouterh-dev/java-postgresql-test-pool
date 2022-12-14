package nl.wouterh.pgpool.junit5;

import nl.wouterh.pgpool.PgPoolConfig;
import nl.wouterh.pgpool.PgPoolManager;
import nl.wouterh.pgpool.PgPoolManagerAdapter;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A JUnit 5 {@link Extension} which integrates {@link PgPoolManager}
 *
 * <pre>
 * {@code
 *   @RegisterExtension
 *   public static final PgPoolExtension pgPoolExtension = new PgPoolExtension(config);
 * }
 * </pre>
 */
public class PgPoolExtension implements PgPoolManagerAdapter, BeforeEachCallback,
    AfterEachCallback {

  private final PgPoolManager manager;

  public PgPoolExtension(PgPoolConfig config) {
    this.manager = new PgPoolManager(config);
  }

  @Override
  public PgPoolManager getManager() {
    return manager;
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    manager.beforeEach();
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    manager.afterEach();
  }
}
