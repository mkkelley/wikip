package wiki;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Michael Kelley on 4/30/14.
 * See LICENSE file for license information.
 */
public class DbConnector {
    public final JdbcTemplate jdbcTemplate;
    public DbConnector(String url) {

//        DataSource pcpds = new PGPoolingDataSource();
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.addDataSourceProperty("serverName", url);
        config.addDataSourceProperty("databaseName", "wikip");
        config.addDataSourceProperty("user", "wikiuser");
        config.addDataSourceProperty("password", "testing");

        jdbcTemplate = new JdbcTemplate(new HikariDataSource(config));
    }
}
