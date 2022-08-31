package nl.wouterh.pgpool.common.example;

import org.testcontainers.containers.PostgreSQLContainer;

public class CommonTestContainer {

  public static final PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:14")
      .withCommand("-N", "2000", "-c", "fsync=off")
      .withReuse(false);
}
