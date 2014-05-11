package wiki.doc;

import wiki.DbConnector;

import java.util.List;
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
    public Integer call() {
        return getIndirection();
    }

    public int getIndirection() {
        if (limit == -1) {
            return -1;
        }
//        System.out.println("getIndirection : searching for " + other.toString() + " in "+ this.toString() +" limit " + limit);
        List<Doc> linkedDocs = start.getLinkedDocs(dbc);
        if (linkedDocs.contains(search)) {
            System.out.println(this.toString() + " links to " + search.toString());
            return 0;
        } else {
            int minIndirection = -1;
            for (Doc d : linkedDocs) {
//                System.out.println(this.toString() + " links to " + d.toString());
                IndirectionCalculator ic = new IndirectionCalculator(d, search, dbc, limit - 1);
                int indirection = ic.call();
                if (indirection == 0) {
                    System.out.println(d.toString() + " links to " + search.toString());
                    return 1;
                }
                if (indirection != -1 && (minIndirection == -1 || indirection < minIndirection)) {
                    minIndirection = indirection;
                    System.out.println("new minInd: " + minIndirection);
                }
            }
            if (minIndirection == -1) return minIndirection;
            else return minIndirection + 1;
        }
    }

}
