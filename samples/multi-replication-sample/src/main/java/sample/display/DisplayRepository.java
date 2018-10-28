package sample.display;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DisplayRepository {
    private final JdbcTemplate template;

    public DisplayRepository(final DataSource displayDataSource) {
        this.template = new JdbcTemplate(displayDataSource);
    }

    @DisplayTransactional(readOnly = true)
    public List<Display> findAll() {
        return template.query("SELECT id, title FROM display", new BeanPropertyRowMapper<>(Display.class));
    }

    @DisplayTransactional
    public void store(final String contents) {
        template.update("INSERT INTO display(title) VALUES (?)", contents);
    }

    public void delete() {
        template.update("DELETE FROM display WHERE title LIKE 'todo%'");
    }
}
