package wiki.result;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Michael Kelley on 5/13/14.
 * See LICENSE file for license information.
 */
public class ResultMapper implements RowMapper<Result> {
    @Override
    public Result mapRow(ResultSet resultSet, int i) throws SQLException {
        long from = resultSet.getLong("fromPage");
        long to = resultSet.getLong("toPage");
        int indirection = resultSet.getInt("indirection");
        int max = resultSet.getInt("max");
        long time = resultSet.getLong("timeTaken");
        return new Result(from, to, indirection, max, time);
    }
}
