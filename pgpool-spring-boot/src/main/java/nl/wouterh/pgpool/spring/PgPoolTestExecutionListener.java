package nl.wouterh.pgpool.spring;

import static nl.wouterh.pgpool.spring.PgPoolDataSource.CLOSED_DATA_SOURCE;

import java.sql.SQLException;
import java.util.stream.Stream;
import javax.sql.DataSource;
import nl.wouterh.pgpool.PgPoolManager;
import nl.wouterh.pgpool.PreparedDatabase;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class PgPoolTestExecutionListener extends AbstractTestExecutionListener {
  @Override
  public void beforeTestMethod(TestContext testContext) throws Exception {
    PgPoolManager manager = manager(testContext);
    manager.beforeEach();

    dataSources(testContext).forEach(ds -> {
      PreparedDatabase preparedDatabase = manager.getPreparedDatabase(ds.getTemplateName());
      if (preparedDatabase == null) {
        throw new NullPointerException(
            "Could not find prepared database for " + ds.getTemplateName());
      }

      // attach it to the delegating dataSource bean
      ds.setTargetDataSource(preparedDatabase.getDataSource());
    });
  }

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    PgPoolManager manager = manager(testContext);

    // detach all the preparedDatabase dataSources from the delegating dataSource beans
    dataSources(testContext).forEach(ds -> {
      ds.setTargetDataSource(CLOSED_DATA_SOURCE);
    });

    manager.afterEach();
  }

  private PgPoolManager manager(TestContext testContext) {
    return testContext.getApplicationContext()
        .getBean(PgPoolManager.class);
  }

  private Stream<PgPoolDataSource> dataSources(TestContext testContext) throws SQLException {
    try {
      return testContext.getApplicationContext()
          .getBeanProvider(DataSource.class).stream()
          .filter(ds -> {
            try {
              return ds.isWrapperFor(PgPoolDataSource.class);
            } catch (SQLException e) {
              throw new RuntimeException(e);
            }
          })
          .map(ds -> {
            try {
              return ds.unwrap(PgPoolDataSource.class);
            } catch (SQLException e) {
              throw new RuntimeException(e);
            }
          });
    } catch (RuntimeException ex) {
      if (ex.getCause() instanceof SQLException) {
        throw (SQLException) ex.getCause();
      }
      throw ex;
    }
  }
}
