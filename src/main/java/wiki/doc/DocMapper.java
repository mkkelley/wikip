package wiki.doc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Michael Kelley on 5/2/14.
 * See LICENSE file for license information.
 */
public class DocMapper implements RowMapper<Doc> {
    @Override
    public Doc mapRow(ResultSet resultSet, int i) throws SQLException {
        long id = resultSet.getLong("id");
        String title = resultSet.getString("title");
//        String text = resultSet.getString("text");
        //TODO: Fix
        return new Doc(id, title, "");
    }
}
