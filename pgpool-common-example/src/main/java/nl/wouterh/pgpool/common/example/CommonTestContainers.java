package nl.wouterh.pgpool.common.example;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Containers used by various tests
 */
public class CommonTestContainers {

  public static final PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:14")
      .withCommand("-N", "2000", "-c", "fsync=off")
      .withReuse(true);
}
