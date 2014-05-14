package wiki.result;

import wiki.DbConnector;
import wiki.Savable;

/**
 * Created by Michael Kelley on 5/13/14.
 * See LICENSE file for license information.
 */
public class Result implements Savable {
    public final long startId;
    public final long searchId;
    public final int indirection;
    public final int max;
    public final long time;

    public Result(long startId, long searchId, int indirection, int max, long time_ms) {
        this.startId = startId;
        this.searchId = searchId;
        this.indirection = indirection;
        this.max = max;
        this.time = time_ms;
    }

    @Override
    public void save(DbConnector dbc) {
        ResultResource.save(this, dbc);
    }
}
