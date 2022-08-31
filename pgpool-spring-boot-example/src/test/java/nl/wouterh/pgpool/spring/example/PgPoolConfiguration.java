package nl.wouterh.pgpool.spring.example;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import nl.wouterh.pgpool.PgPoolConfig;
import nl.wouterh.pgpool.PooledDatabase;
import nl.wouterh.pgpool.common.example.CommonTestContainer;
import nl.wouterh.pgpool.liquibase.LiquibaseDatabaseInitializer;
import nl.wouterh.pgpool.spring.PgPoolDataSource;
import nl.wouterh.pgpool.spring.PgPoolDataSourceInitializerOverride;
import nl.wouterh.pgpool.spring.hikari.HikariDataSourceFactory;
import nl.wouterh.pgpool.testcontainers.PostgreSQLContainerConnectionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.JdbcDatabaseContainer;

@Configuration
public class PgPoolConfiguration {

  @Bean
  ExecutorService executorService() {
    return Executors.newCachedThreadPool();
  }

  @Bean
  PgPoolConfig pgPoolConfig(
      SpringBeanTableFiller tableFiller,
      DataSource dataSource,
      ExecutorService executorService
  ) throws SQLException {
    // Optionally start container concurrently, without blocking the spring context initialization
    Future<JdbcDatabaseContainer> postgresContainer = executorService.submit(() -> {
      CommonTestContainer.postgres.start();
      return CommonTestContainer.postgres;
    });

    return PgPoolConfig.builder()
        .executor(executorService)
        .connectionProvider(new PostgreSQLContainerConnectionProvider(postgresContainer))
        .waitForDropOnShutdown(true)
        .dropThreads(2)
        .pooledDatabase(PooledDatabase.builder()
            .name("db1")
            .createThreads(2)
            .spares(10)
            .listener(new HikariDataSourceFactory())
            .initializer(new LiquibaseDatabaseInitializer("db/changelog/changelog.xml"))
            .initializer(new PgPoolDataSourceInitializerOverride(dataSource, tableFiller))
            .build())
        .build();
  }

  @Bean
  DataSource dataSource() {
    return new PgPoolDataSource("db1");
  }
}
