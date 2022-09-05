package nl.wouterh.pgpool.spring;

import lombok.extern.slf4j.Slf4j;
import nl.wouterh.pgpool.PgPoolConfig;
import nl.wouterh.pgpool.PgPoolManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for {@link PgPoolManager}. Requires a {@link PgPoolConfig} bean to be configured.
 *
 * @see PgPoolTest
 * @see PgPoolConfig
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class PgPoolAutoConfiguration {

  @Bean
  @ConditionalOnBean(PgPoolConfig.class)
  @ConditionalOnMissingBean(PgPoolManager.class)
  public PgPoolManager pgPoolManager(PgPoolConfig config) {
    PgPoolManager manager = new PgPoolManager(config);
    manager.scheduleStart();

    return manager;
  }

}
