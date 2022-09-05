package nl.wouterh.pgpool.spring.hikari;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import nl.wouterh.pgpool.PreparedDatabase;
import nl.wouterh.pgpool.PreparedDatabaseDataSource;
import nl.wouterh.pgpool.PreparedDatabaseDataSourceFactory;

/**
 * Creates a {@link HikariDataSource} for a {@link PreparedDatabase}
 */
public class HikariDataSourceFactory extends PreparedDatabaseDataSourceFactory<HikariDataSource> {

  public static HikariConfig defaultConfig() {
    HikariConfig config = new HikariConfig();
    // start with just 1 connection instead of the default 10. Typical tests only need 1 connection total
    config.setMinimumIdle(1);
    // database is meant to be local & ready, so reduce timeout from 30s to 5s
    config.setConnectionTimeout(SECONDS.toMillis(5));
    return config;
  }

  private final HikariConfig config;

  public HikariDataSourceFactory() {
    this(defaultConfig());
  }

  public HikariDataSourceFactory(HikariConfig config) {
    super(HikariDataSource.class);
    this.config = config;
  }

  @Override
  protected HikariDataSource createDataSource(PreparedDatabase preparedDatabase) {
    HikariConfig dbConfig = new HikariConfig();
    this.config.copyStateTo(dbConfig);
    dbConfig.setDataSource(new PreparedDatabaseDataSource(preparedDatabase));

    return new HikariDataSource(dbConfig);
  }

  @Override
  protected void closeDataSource(PreparedDatabase preparedDatabase, HikariDataSource dataSource) {
    dataSource.close();
  }

}
