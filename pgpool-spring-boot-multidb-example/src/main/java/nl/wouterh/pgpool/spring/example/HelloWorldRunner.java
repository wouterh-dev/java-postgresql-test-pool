package nl.wouterh.pgpool.spring.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class HelloWorldRunner implements CommandLineRunner {

  private final SampleService sampleService;

  public HelloWorldRunner(SampleService sampleService) {
    this.sampleService = sampleService;
  }

  @Override
  public void run(String... args) throws Exception {
    sampleService.selectAll();
  }
}
