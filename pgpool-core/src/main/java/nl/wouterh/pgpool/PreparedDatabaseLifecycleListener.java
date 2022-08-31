package nl.wouterh.pgpool;

/**
 * Listens to the lifecycle of the {@link PreparedDatabase}s. Called from the create & drop threads,
 * not from the test thread
 *
 * @see PreparedDatabaseDataSourceFactory
 */
public interface PreparedDatabaseLifecycleListener {

  /**
   * Called from a create thread after a {@link PreparedDatabase} has been created from a template
   *
   * @param preparedDatabase the database that has been created
   */
  default void afterCreate(PreparedDatabase preparedDatabase) throws Exception {

  }

  /**
   * Called from a drop threads before a {@link PreparedDatabase} will be dropped
   *
   * @param preparedDatabase the database that has been created
   */
  default void beforeDrop(PreparedDatabase preparedDatabase) throws Exception {

  }
}
