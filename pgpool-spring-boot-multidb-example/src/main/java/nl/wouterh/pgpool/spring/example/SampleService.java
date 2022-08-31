package nl.wouterh.pgpool.spring.example;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SampleService {

  private final JdbcTemplate jdbcTemplate;

  public SampleService(
      @Qualifier("db1") JdbcTemplate jdbcTemplate
  ) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Transactional(transactionManager = "transactionManager1")
  public List<String> selectAll() {
    return jdbcTemplate.query("SELECT test_column FROM test_table", (rs, rowNum) -> {
      return rs.getString("test_column");
    });
  }

  @Transactional(transactionManager = "transactionManager1")
  public int delete(String value, boolean triggerRollback) {
    int affected = jdbcTemplate.update("DELETE FROM test_table WHERE test_column = ?", value);
    if (triggerRollback) {
      throw new IllegalStateException("Triggered rollback!");
    }

    return affected;
  }
}
