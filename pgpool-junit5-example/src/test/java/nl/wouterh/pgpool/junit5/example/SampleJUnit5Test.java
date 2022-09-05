package nl.wouterh.pgpool.junit5.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import nl.wouterh.pgpool.PooledDatabase;
import nl.wouterh.pgpool.PgPoolConfig;
import nl.wouterh.pgpool.common.example.CommonTestContainers;
import nl.wouterh.pgpool.common.example.ExampleTableCreator;
import nl.wouterh.pgpool.common.example.ExampleTableFiller;
import nl.wouterh.pgpool.junit5.PgPoolExtension;
import nl.wouterh.pgpool.testcontainers.PostgreSQLContainerConnectionProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.utility.TestcontainersConfiguration;

@Slf4j
public class SampleJUnit5Test {
  static {
    CommonTestContainers.postgres.start();
  }

  @RegisterExtension
  public static final PgPoolExtension pgPoolExtension = new PgPoolExtension(
      PgPoolConfig.builder()
          .connectionProvider(new PostgreSQLContainerConnectionProvider(CommonTestContainers.postgres))
          .waitForDropOnShutdown(
              TestcontainersConfiguration.getInstance().environmentSupportsReuse())
          .pooledDatabase(PooledDatabase.builder()
              .name("db1")
              .createThreads(2)
              .spares(5)
              .initializer(new ExampleTableCreator())
              .initializer(new ExampleTableFiller())
              .build())
          .pooledDatabase(PooledDatabase.builder()
              .name("db2")
              .build())
          .build());

  private void test() throws Exception {
    try (Connection connection = pgPoolExtension.createConnection("db1")) {
      try (Statement statement = connection.createStatement();
          ResultSet rs = statement.executeQuery("SELECT current_database()")) {
        rs.next();
        log.info("{}", rs.getString(1));
      }
    }
  }

  @Test
  public void test1() throws Exception {
    test();
  }

  @Test
  public void test2() throws Exception {
    test();
  }

}
