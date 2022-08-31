package nl.wouterh.pgpool.spring.example;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties(JdbcProperties.class)
public class MultiDbConfiguration {

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource")
  DataSourceProperties dataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Qualifier("db1")
  @ConfigurationProperties(prefix = "spring.db1.datasource")
  @Profile("!test")
  DataSourceProperties dataSourceProperties1() {
    return new DataSourceProperties();
  }

  @Bean
  @Qualifier("db1")
  @ConfigurationProperties(prefix = "spring.db1.datasource.hikari")
  @Profile("!test")
  DataSource dataSource1(@Qualifier("db1") DataSourceProperties dataSourceProperties) {
    return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
  }

  @Bean
  @Qualifier("db1")
  @ConfigurationProperties(prefix = "spring.db1.liquibase")
  LiquibaseProperties liquibaseProperties1() {
    return new LiquibaseProperties();
  }

  @Bean
  @Qualifier("db1")
  @Profile("!test")
  SpringLiquibase liquibase1(
      @Qualifier("db1") DataSource dataSource,
      @Qualifier("db1") LiquibaseProperties properties
  ) {
    return createSpringLiquibase(dataSource, properties);
  }

  @Bean
  @Qualifier("db1")
  PlatformTransactionManager transactionManager1(@Qualifier("db1") DataSource dataSource) {
    return new JdbcTransactionManager(dataSource);
  }

  @Bean
  @Qualifier("db1")
  JdbcTemplate jdbcTemplate1(@Qualifier("db1") DataSource dataSource, JdbcProperties properties) {
    return createJdbcTemplate(dataSource, properties);
  }

  @Bean
  @Qualifier("db2")
  @ConfigurationProperties(prefix = "spring.db2.datasource")
  @Profile("!test")
  DataSourceProperties dataSourceProperties2() {
    return new DataSourceProperties();
  }

  @Bean
  @Qualifier("db2")
  @ConfigurationProperties(prefix = "spring.db2.datasource.hikari")
  @Profile("!test")
  DataSource dataSource2(@Qualifier("db2") DataSourceProperties dataSourceProperties) {
    return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
  }

  @Bean
  @Qualifier("db2")
  @ConfigurationProperties(prefix = "spring.db2.liquibase")
  LiquibaseProperties liquibaseProperties2() {
    return new LiquibaseProperties();
  }

  @Bean
  @Qualifier("db2")
  @Profile("!test")
  SpringLiquibase liquibase2(
      @Qualifier("db2") DataSource dataSource,
      @Qualifier("db2") LiquibaseProperties properties
  ) {
    return createSpringLiquibase(dataSource, properties);
  }

  @Bean
  @Qualifier("db2")
  PlatformTransactionManager transactionManager2(@Qualifier("db2") DataSource dataSource) {
    return new JdbcTransactionManager(dataSource);
  }

  @Bean
  @Qualifier("db2")
  JdbcTemplate jdbcTemplate2(@Qualifier("db2") DataSource dataSource, JdbcProperties properties) {
    return createJdbcTemplate(dataSource, properties);
  }

  public static JdbcTemplate createJdbcTemplate(DataSource dataSource, JdbcProperties properties) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    JdbcProperties.Template template = properties.getTemplate();
    jdbcTemplate.setFetchSize(template.getFetchSize());
    jdbcTemplate.setMaxRows(template.getMaxRows());
    if (template.getQueryTimeout() != null) {
      jdbcTemplate.setQueryTimeout((int) template.getQueryTimeout().getSeconds());
    }
    return jdbcTemplate;
  }

  private static SpringLiquibase createSpringLiquibase(DataSource dataSource,
      LiquibaseProperties properties) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(properties.getChangeLog());
    liquibase.setClearCheckSums(properties.isClearChecksums());
    liquibase.setContexts(properties.getContexts());
    liquibase.setDefaultSchema(properties.getDefaultSchema());
    liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
    liquibase.setLiquibaseTablespace(properties.getLiquibaseTablespace());
    liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
    liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
    liquibase.setDropFirst(properties.isDropFirst());
    liquibase.setShouldRun(properties.isEnabled());
    liquibase.setLabels(properties.getLabels());
    liquibase.setChangeLogParameters(properties.getParameters());
    liquibase.setRollbackFile(properties.getRollbackFile());
    liquibase.setTestRollbackOnUpdate(properties.isTestRollbackOnUpdate());
    liquibase.setTag(properties.getTag());
    return liquibase;
  }
}
