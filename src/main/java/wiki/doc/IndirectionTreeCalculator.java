package wiki.doc;

import wiki.DbConnector;
import wiki.result.Result;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by Michael Kelley on 8/6/14.
 * See LICENSE file for license information.
 */
public class IndirectionTreeCalculator implements Callable<ArrayList<Result>> {
    protected final DocId start;
    protected final DbConnector dbc;
    protected final int limit;
    public IndirectionTreeCalculator(DocId start, DbConnector dbc, int limit) {
        this.start = start;
        this.dbc = dbc;
        this.limit = limit;
    }
    @Override
    public ArrayList<Result> call() throws Exception {
        if (limit == -1) return null;

        Map<Integer, List<DocId>> linksHash = new HashMap<>();
        ArrayList<Result> results = new ArrayList<>();

        List<DocId> initialList = new ArrayList<>();
        initialList.add(start);

        linksHash.put(0, initialList);

        for (int i = 1; i <= limit; i++) {
            List<DocId> linkedDocs = DocResource.getAllLinkedDocs(linksHash.get(i - 1), dbc);
            System.out.println("i = " + i + " ldc: " + linkedDocs.size());
            linksHash.put(i, linkedDocs);
            for (DocId did : linkedDocs) {
                results.add(new Result(start.id, did.id, i, limit, -1));
            }
        }
        return results;
    }
}
