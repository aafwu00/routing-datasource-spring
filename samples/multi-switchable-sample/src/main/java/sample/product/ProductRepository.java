package sample.product;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
    private final JdbcTemplate template;

    public ProductRepository(DataSource productDataSource) {
        this.template = new JdbcTemplate(productDataSource);
    }

    public List<Product> findAll() {
        return template.query("SELECT id, title FROM product", new BeanPropertyRowMapper<>(Product.class));
    }
}
