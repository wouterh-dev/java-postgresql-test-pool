package nl.wouterh.pgpool.spring.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import nl.wouterh.pgpool.DatabaseInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpringBeanTableFiller implements DatabaseInitializer {

  @Autowired
  private SampleService sampleService;

  @Override
  public byte[] calculateChecksum() throws IOException {
    return (SpringBeanTableFiller.class.getName() + "v0").getBytes(StandardCharsets.UTF_8);
  }

  public void run(Connection ignoredConnection) throws SQLException {
    for (int i = 0; i < 100; i++) {
      sampleService.insert(i);
    }
  }

}
