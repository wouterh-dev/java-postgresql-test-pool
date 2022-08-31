package nl.wouterh.pgpool.spring;

import lombok.extern.slf4j.Slf4j;
import nl.wouterh.pgpool.PgPoolConfig;
import nl.wouterh.pgpool.PgPoolManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class PgPoolAutoConfiguration {

  @Bean
  public PgPoolManager pgPoolManager(PgPoolConfig config) {
    PgPoolManager manager = new PgPoolManager(config);
    manager.scheduleStart();

    return manager;
  }

}
