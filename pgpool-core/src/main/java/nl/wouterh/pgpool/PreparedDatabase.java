package nl.wouterh.pgpool;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A prepared database which has been created using a template database
 */
@RequiredArgsConstructor
public class PreparedDatabase {

  /**
   * The name of the prepared database
   */
  @Getter
  private final String name;

  /**
   * The {@link PooledDatabase} used to create this prepared database
   */
  @Getter
  private final PooledDatabase pooledDatabase;
  @Getter
  private final ConnectionProvider connectionProvider;

  /**
   * Whether a connection to this prepared database has been requested. If a prepared database is
   * not dirty it may be reused for a future test execution.
   */
  @Getter
  private boolean dirty;

  @Getter
  private DataSource dataSource = new DirtyFlaggingDataSource<>(this,
      new PreparedDatabaseDataSource(this));

  /**
   * Metadata associated with the prepared database.
   *
   * @see PreparedDatabaseLifecycleListener
   */
  private final Map<String, Object> metadata = new HashMap<>();

  /**
   * @see DirtyFlaggingDataSource
   */
  public void flagDirty() {
    dirty = true;
  }

  public void setDataSource(DataSource ds, boolean wrapFlagDirty) {
    if (wrapFlagDirty) {
      ds = new DirtyFlaggingDataSource<>(this, ds);
    }

    this.dataSource = ds;
  }

  @SuppressWarnings("unchecked")
  public <T> T getMetadata(String key) {
    return (T) metadata.get(key);
  }

  public <T> void setMetadata(String key, T value) {
    metadata.put(key, value);
  }
}
