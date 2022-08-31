package nl.wouterh.pgpool;

import java.util.Collections;
import java.util.List;

public class CompositePreparedDatabaseLifecycleListener implements PreparedDatabaseLifecycleListener {

  private final List<PreparedDatabaseLifecycleListener> listeners;

  public CompositePreparedDatabaseLifecycleListener(
      List<PreparedDatabaseLifecycleListener> listeners) {
    this.listeners = listeners != null ? listeners : Collections.emptyList();
  }

  @Override
  public void afterCreate(PreparedDatabase preparedDatabase) throws Exception {
    for (PreparedDatabaseLifecycleListener listener : listeners) {
      listener.afterCreate(preparedDatabase);
    }
  }

  @Override
  public void beforeDrop(PreparedDatabase preparedDatabase) throws Exception {
    for (int i = listeners.size() - 1; i >= 0; i--) {
      PreparedDatabaseLifecycleListener listener = listeners.get(i);
      listener.beforeDrop(preparedDatabase);
    }
  }
}
