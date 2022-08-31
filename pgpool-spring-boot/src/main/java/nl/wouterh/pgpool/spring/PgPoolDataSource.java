package nl.wouterh.pgpool.spring;

import javax.sql.DataSource;
import lombok.Getter;
import org.springframework.jdbc.datasource.DelegatingDataSource;

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
