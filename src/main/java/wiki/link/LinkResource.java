package wiki.link;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import wiki.DbConnector;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Michael Kelley on 5/2/14.
 * See LICENSE file for license information.
 */
public class LinkResource {
    public static void save(Link link, DbConnector dbc) {
        List<Link> pages = dbc.jdbcTemplate.query("SELECT * FROM links WHERE toPage = ? AND fromPage = ?",
                new Object[]{link.to, link.from},
                new LinkMapper());
        if (pages.size() == 0) {
            dbc.jdbcTemplate.update("INSERT INTO links(frompage, topage) values(?, ?)", link.from, link.to);
        }
    }

    public static void insertAll(List<Link> links, DbConnector dbc) {
        BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setLong(1, links.get(i).from);
                preparedStatement.setLong(2, links.get(i).to);
            }

            @Override
            public int getBatchSize() {
                return links.size();
            }
        };

        try {
            dbc.jdbcTemplate.batchUpdate("INSERT INTO links(fromPage, toPage) values(?, ?)", bpss);
        } catch (BadSqlGrammarException e) {
            e.printStackTrace();
            BatchUpdateException bue = (BatchUpdateException)e.getCause();
            System.out.println(bue.getNextException());
            System.exit(1);
        }
    }
}

