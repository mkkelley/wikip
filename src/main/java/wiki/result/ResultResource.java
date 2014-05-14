package wiki.result;

import wiki.DbConnector;

import java.util.List;
import java.util.Optional;

/**
 * Created by Michael Kelley on 5/13/14.
 * See LICENSE file for license information.
 */
public class ResultResource {
    public static void save(Result result, DbConnector dbc) {
        dbc.jdbcTemplate.update("INSERT INTO results(fromPage, toPage, indirection, max, timeTaken) values(?, ?, ?, ?, ?)",
                result.startId, result.searchId, result.indirection, result.max, result.time);
    }

    private static ResultMapper rm = new ResultMapper();
    public static Optional<Result> load(long from, long to, long max, DbConnector dbc) {
        String sql = "SELECT * FROM results WHERE fromPage = ? AND toPage = ? AND max = ?";
        List<Result> results = dbc.jdbcTemplate.query(sql, new Object[]{from, to, max}, rm);
        Optional<Result> highestExisting = results.stream().max((result, result2) -> result.max - result2.max);
        if (!highestExisting.isPresent()) {
            return Optional.empty();
        } else if (max <= highestExisting.get().max) {
            return highestExisting;
        }
        return Optional.empty();
    }
}
