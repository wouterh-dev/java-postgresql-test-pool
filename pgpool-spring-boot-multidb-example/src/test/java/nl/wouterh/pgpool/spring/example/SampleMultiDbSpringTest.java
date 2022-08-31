package nl.wouterh.pgpool.spring.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.IntStream;
import nl.wouterh.pgpool.spring.PgPoolTest;
import nl.wouterh.pgpool.spring.PgPoolTestExecutionListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;

@SpringBootTest(properties = {
    "spring.liquibase.enabled=false"
})
@ActiveProfiles("test")
@PgPoolTest
public class SampleMultiDbSpringTest {

  @Autowired
  SampleService sampleService;

  private void test() {
    assertEquals(100, sampleService.selectAll().size());

    assertEquals(1, sampleService.delete("Example 1", false));
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
