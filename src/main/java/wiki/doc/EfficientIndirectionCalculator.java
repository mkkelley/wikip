package wiki.doc;

import wiki.DbConnector;
import wiki.result.Result;
import wiki.result.ResultResource;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by Michael Kelley on 5/10/14.
 * See LICENSE file for license information.
 */
public class EfficientIndirectionCalculator implements Callable<Result> {
    private final Doc start;
    private final Doc search;
    private final DbConnector dbc;
    private final int limit;
    public EfficientIndirectionCalculator(Doc start, Doc search, DbConnector dbc, int limit) {
        this.start = start;
        this.search = search;
        this.dbc = dbc;
        this.limit = limit;
    }

    @Override
    public Result call() {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        System.out.println(ts + " Getting indirection between " + start + " and " + search);
        Optional<Result> result = ResultResource.load(start.id, search.id, limit, dbc);
        if (result.isPresent()) {
            System.out.println("Indirection already calculated: " + result.get().indirection);
            return result.get();
        }
        long millis = System.currentTimeMillis();
        int indirection = getIndirectionEfficient();
        long dtime = System.currentTimeMillis() - millis;
        ts = new Timestamp(System.currentTimeMillis());
        System.out.println(ts + " Indirection of " + indirection + " from " + start + " to " + search + " took " + dtime);
        return new Result(start.id, search.id, indirection, limit, dtime);
    }

    private int getIndirectionEfficient() {
        int levelsFromThis = limit / 2 + limit % 2;
        int levelsFromOther = limit / 2;
        Map<Integer, List<DocId>> fromThis = new HashMap<>();
        Map<Integer, List<DocId>> fromOther = new HashMap<>();

        List<DocId> thisList = new ArrayList<>();
        thisList.add(start);
        List<DocId> otherList = new ArrayList<>();
        otherList.add(search);

        fromThis.put(0, thisList);
        fromOther.put(0, otherList);

        for (int i = 1; i <= levelsFromThis; i++) {
            List<DocId> fromDocs = fromThis.get(i - 1);
            List<DocId> toDocs = DocResource.getAllLinkedDocs(fromDocs, dbc);
            fromThis.put(i, toDocs);
        }
        for (int i = 1; i <= levelsFromOther; i++) {
            List<DocId> toDocs = fromOther.get(i - 1);
            List<DocId> fromDocs = DocResource.getAllLinking(toDocs, dbc);
            fromOther.put(i, fromDocs);
        }

        int min = limit + 1;
        for (int otherIndex = 0; otherIndex < levelsFromOther + 1; otherIndex++) {
            for (int thisIndex = 0; thisIndex < levelsFromThis + 1; thisIndex++) {
                if (otherIndex + thisIndex >= min) continue;
                if (otherIndex == levelsFromOther) {
                    List<DocId> fromDocs = fromThis.get(thisIndex);
                    List<DocId> targetTargetDocs = fromOther.get(otherIndex);

                    for (int i = 0; i <= targetTargetDocs.size() / 15000; i++) {
                        if (otherIndex + thisIndex >= min) continue;
                        int from = i * 15000;
                        int to = (i + 1) * 15000;
                        if (to > targetTargetDocs.size()) {
                            to = targetTargetDocs.size();
                        }
                        List<DocId> ttdsl = targetTargetDocs.subList(from, to);
                        List<DocId> targetDocs = DocResource.getAllLinking(ttdsl, dbc);
                        if (!Collections.disjoint(fromDocs, targetDocs)) {
                            min = thisIndex + otherIndex;
                        }
                    }
                } else {
                    List<DocId> fromDocs = fromThis.get(thisIndex);
                    List<DocId> targetDocs = fromOther.get(otherIndex + 1);
                    if (!Collections.disjoint(fromDocs, targetDocs)) {
                        min = thisIndex + otherIndex;
                    }
                }
            }
        }
        if (min == limit + 1) return -1;
        return min;
    }

}
