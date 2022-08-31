package nl.wouterh.pgpool.spring.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.IntStream;
import nl.wouterh.pgpool.spring.PgPoolTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * @see PgPoolConfiguration
 */
// Disable Spring Boot's liquibase migrations since PgPool handles this
@SpringBootTest(properties = {
    "spring.liquibase.enabled=false"
})
// Activate PgPool
@PgPoolTest
// Stop HelloWorldRunner from running during tests (PgPool itself does not rely on the test profile)
@ActiveProfiles("test")
public class SampleSpringTest {

  @Autowired
  SampleService sampleService;

  @Autowired
  JdbcTemplate jdbcTemplate;

  private void test() {
    assertEquals(100, sampleService.selectAll().size());

    assertEquals(1, sampleService.delete("Example 1", false));

    // test spring boot Transactional rollback
    assertThrows(IllegalStateException.class, () -> sampleService.delete("Example 2", true));

    assertEquals(99, sampleService.selectAll().size());
  }

  @Test
  public void test1() throws Exception {
    test();
  }

  @Test
  public void test2() throws Exception {
    test();
  }

  @ParameterizedTest
  @MethodSource("test3_parameters")
  public void test3(int idx) throws Exception {
    test();
  }

  public static int[] test3_parameters() {
    return IntStream.range(0, 100).toArray();
  }
}
