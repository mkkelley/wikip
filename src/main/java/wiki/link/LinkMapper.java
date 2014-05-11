package wiki.link;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LinkMapper implements RowMapper<Link> {

    @Override
    public Link mapRow(ResultSet resultSet, int i) throws SQLException {
        long to = resultSet.getLong("fromPage");
        long from = resultSet.getLong("toPage");
        return new Link(from, to);
    }
}
