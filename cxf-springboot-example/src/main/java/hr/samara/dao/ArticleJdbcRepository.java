package hr.samara.dao;

import hr.samara.model.Article;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ArticleJdbcRepository implements ArticleRepository {

    private static final String SELECT = "SELECT id, name, price, created FROM samara.article ORDER BY id DESC";
    private static final String SELECT_BY_NAME = "SELECT * FROM samara.article WHERE name=?";
    private static final String UPDATE_PRICE = "UPDATE samara.article SET price = ?";
    private static final String INSERT = "INSERT INTO samara.article (name, price, created) VALUES (?,  ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    ArticleJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Article> findAll() {
        return jdbcTemplate.query(SELECT,
                (rs, rowNum) -> new Article(rs.getLong("id"), rs.getString("name"), rs.getBigDecimal("price"), rs.getDate("created"))
        );
    }

    @Override
    public Article findByName(String name) {
        return jdbcTemplate.queryForObject(SELECT_BY_NAME, new Object[]{name},
                new BeanPropertyRowMapper<Article>(Article.class));
    }

    @Override
    public void updatePrice(Long id, BigDecimal price) {
        jdbcTemplate.update(UPDATE_PRICE, price);
    }

    @Override
    public Article save(Article article) {
        final PreparedStatementCreator psc = connection -> {
            // ID is column generated by database - usingGeneratedKeyColumns
            final PreparedStatement ps = connection.prepareStatement(INSERT, new String[]{"ID"});
            populatePrepareStatement(article, ps);
            return ps;
        };
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(psc, holder);
        Long id = holder.getKey().longValue();
        article.setId(id);
        return article;
    }

    private void populatePrepareStatement(Article article, PreparedStatement ps) throws SQLException {
        ps.setString(1, article.getName());
        ps.setBigDecimal(2, article.getPrice());
        ps.setTimestamp(3, new Timestamp(article.getCreated().getTime()));
    }

}
