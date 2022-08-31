package nl.wouterh.pgpool;

import javax.sql.DataSource;

/**
 * Creates & associates a {@link DataSource} with a {@link PreparedDatabase}
 *
 * @param <DS> the DataSource type used
 */
public abstract class PreparedDatabaseDataSourceFactory<DS extends DataSource> implements
    PreparedDatabaseLifecycleListener {

  private final Class<DS> clazz;

  protected PreparedDatabaseDataSourceFactory(Class<DS> clazz) {
    this.clazz = clazz;
  }

  @Override
  public final void afterCreate(PreparedDatabase preparedDatabase) throws Exception {
    DS dataSource = createDataSource(preparedDatabase);
    preparedDatabase.setDataSource(dataSource, true);
  }

  @Override
  public final void beforeDrop(PreparedDatabase preparedDatabase) throws Exception {
    DS ds = preparedDatabase.getDataSource().unwrap(clazz);
    closeDataSource(preparedDatabase, ds);
  }

  protected abstract DS createDataSource(PreparedDatabase preparedDatabase) throws Exception;

  protected abstract void closeDataSource(PreparedDatabase preparedDatabase, DS dataSource)
      throws Exception;
}
