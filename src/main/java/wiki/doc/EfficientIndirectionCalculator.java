package wiki.doc;

import wiki.DbConnector;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by Michael Kelley on 5/10/14.
 * See LICENSE file for license information.
 */
public class EfficientIndirectionCalculator implements Callable<Integer> {
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
    public Integer call() {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        System.out.println(ts + " Getting indirection between " + start + " and " + search);
        long millis = System.currentTimeMillis();
        int indirection = getIndirectionEfficient();
        ts = new Timestamp(System.currentTimeMillis());
        System.out.println(ts + " Indirection of " + indirection + " from " + start + " to " + search + " took " + (System.currentTimeMillis() - millis));
        return indirection;
    }

    private int getIndirectionEfficient() {
        int levelsFromThis = limit / 2 + limit % 2;
        int levelsFromOther = limit / 2 + 1;
        Map<Integer, List<Doc>> fromThis = new HashMap<>();
        Map<Integer, List<Doc>> fromOther = new HashMap<>();

        List<Doc> thisList = new ArrayList<>();
        thisList.add(start);
        List<Doc> otherList = new ArrayList<>();
        otherList.add(search);

        fromThis.put(0, thisList);
        fromOther.put(0, otherList);

        for (int i = 1; i <= levelsFromThis; i++) {
            List<Doc> fromDocs = fromThis.get(i - 1);
            List<Doc> toDocs = DocResource.getAllLinkedDocs(fromDocs, dbc);
            fromThis.put(i, toDocs);
        }
        for (int i = 1; i <= levelsFromOther; i++) {
            List<Doc> toDocs = fromOther.get(i - 1);
            List<Doc> fromDocs = DocResource.getAllLinking(toDocs, dbc);
            fromOther.put(i, fromDocs);
        }

        int min = limit + 1;
        for (int thisIndex = 0; thisIndex < levelsFromThis + 1; thisIndex++) {
            for (int otherIndex = 0; otherIndex < levelsFromOther; otherIndex++) {
                if (otherIndex + thisIndex >= min) continue;
                List<Doc> fromDocs = fromThis.get(thisIndex);
                List<Doc> targetDocs = fromOther.get(otherIndex + 1);
                for (Doc target : targetDocs) {
                    if (fromDocs.contains(target) && otherIndex + thisIndex < min) {
//                        System.out.println("Link ("+thisIndex+") found to " + target.title + " (" +otherIndex+")");
                        min = thisIndex + otherIndex;
//                        return thisIndex + otherIndex;
                    }
                }
            }
        }
        if (min == limit + 1) return -1;
        return min;
    }

}
