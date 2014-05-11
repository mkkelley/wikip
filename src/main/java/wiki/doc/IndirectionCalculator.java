package wiki.doc;

import wiki.DbConnector;

import java.util.concurrent.Callable;

/**
 * Created by Michael Kelley on 5/2/14.
 * See LICENSE file for license information.
 */
public class IndirectionCalculator implements Callable<Integer> {
    protected final Doc start;
    protected final Doc search;
    protected final DbConnector dbc;
    protected final int limit;
    public IndirectionCalculator(Doc start, Doc search, DbConnector dbc, int limit) {
        this.start = start;
        this.search = search;
        this.dbc = dbc;
        this.limit = limit;
    }
    @Override
    public Integer call() throws Exception {
        return start.getIndirection(search, dbc, limit);
    }
}
