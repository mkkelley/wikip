package wiki.doc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Michael Kelley on 5/11/14.
 * See LICENSE file for license information.
 */
public class DocIdMapper implements RowMapper<DocId> {
    @Override
    public DocId mapRow(ResultSet resultSet, int i) throws SQLException {
        long id = resultSet.getLong("id");
        return new DocId(id);
    }
}
