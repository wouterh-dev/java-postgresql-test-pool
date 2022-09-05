package nl.wouterh.pgpool.spring;

import javax.sql.DataSource;
import lombok.Getter;
import nl.wouterh.pgpool.DatabaseInitializer;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * A {@link DelegatingDataSource} which allows for a {@link ThreadLocal} override. Useful for
 * running {@link DatabaseInitializer} which use Spring wired {@link DataSource}s, either directly
 * or indirectly.
 *
 * @see PgPoolDataSourceInitializerOverride
 */
public class PgPoolDataSource extends DelegatingDataSource {

  static final ClosedDataSource CLOSED_DATA_SOURCE = new ClosedDataSource();

  private final ThreadLocal<DataSource> threadLocalDataSourceOverride = new ThreadLocal<>();

  public void setThreadLocalOverride(DataSource ds) {
    threadLocalDataSourceOverride.set(ds);
  }

  @Getter
  private final String templateName;

  public PgPoolDataSource(String templateName) {
    super(CLOSED_DATA_SOURCE);
    this.templateName = templateName;
  }

  @Override
  public DataSource getTargetDataSource() {
    DataSource ds = threadLocalDataSourceOverride.get();
    if (ds != null) {
      return ds;
    }

    return super.getTargetDataSource();
  }
}
