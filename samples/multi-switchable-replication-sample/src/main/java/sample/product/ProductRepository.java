package sample.product;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProductRepository {
    private final JdbcTemplate template;

    public ProductRepository(DataSource productDataSource) {
        this.template = new JdbcTemplate(productDataSource);
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return template.query("SELECT id, title FROM product", new BeanPropertyRowMapper<>(Product.class));
    }

    public void store(final String contents) {
        template.update("INSERT INTO product(title) VALUES (?)", contents);
    }

    public void delete() {
        template.update("DELETE FROM product WHERE title LIKE 'todo%'");
    }
}
