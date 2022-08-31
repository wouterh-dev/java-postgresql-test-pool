package nl.wouterh.pgpool.spring.example;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SampleService {

  private final JdbcTemplate jdbcTemplate;

  public SampleService(
      JdbcTemplate jdbcTemplate
  ) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<String> selectAll() {
    return jdbcTemplate.query("SELECT test_column FROM test_table", (rs, rowNum) -> {
      return rs.getString("test_column");
    });
  }

  @Transactional
  public int delete(String value, boolean triggerRollback) {
    int affected = jdbcTemplate.update("DELETE FROM test_table WHERE test_column = ?", value);
    if (triggerRollback) {
      throw new IllegalStateException("Triggered rollback!");
    }

    return affected;
  }

  public void insert(int id) {
    jdbcTemplate.update(
        "INSERT INTO test_table(test_id, test_column) VALUES (?, ?)",
        id,
        "Example " + id
    );
  }
}
