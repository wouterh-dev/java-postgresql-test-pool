package nl.wouterh.pgpool.junit4;


import nl.wouterh.pgpool.PgPoolConfig;
import nl.wouterh.pgpool.PgPoolManager;
import nl.wouterh.pgpool.PgPoolManagerAdapter;
import org.junit.rules.ExternalResource;

public class PgPoolRule extends ExternalResource implements PgPoolManagerAdapter {

  private final PgPoolManager manager;

  public PgPoolRule(PgPoolConfig config) {
    this.manager = new PgPoolManager(config);
  }

  @Override
  public PgPoolManager getManager() {
    return manager;
  }

  @Override
  protected void before() throws Throwable {
    manager.beforeEach();
  }

  @Override
  protected void after() {
    try {
      manager.afterEach();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
