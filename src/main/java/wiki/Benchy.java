package wiki;

import wiki.doc.Doc;
import wiki.doc.DocId;
import wiki.doc.DocResource;
import wiki.doc.EfficientIndirectionCalculator;
import wiki.result.Result;
import wiki.result.ResultResource;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Michael Kelley on 5/11/14.
 * See LICENSE file for license information.
 */
public class Benchy {
    public static void main(String[] args) {
        DbConnector dbc = new DbConnector("localhost");
        Doc start = DocResource.getDoc(789980L, dbc);
        Doc search = DocResource.getDoc(3026593L, dbc);
        int limit = 6;
//        EfficientIndirectionCalculator eic = new EfficientIndirectionCalculator(one, two, dbc, 6);
//        eic.call();
//        EfficientIndirectionCalculator eic2 = new EfficientIndirectionCalculator(one, two, dbc, 6);
//        eic2.call();
        System.out.println();
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        System.out.println(ts + " Getting indirection between " + start + " and " + search);
        long millis = System.currentTimeMillis();
        int indirection = getIndirectionEfficient(start, search, limit, dbc);
        long dtime = System.currentTimeMillis() - millis;
        ts = new Timestamp(System.currentTimeMillis());
        System.out.println(ts + " Indirection of " + indirection + " from " + start + " to " + search + " took " + dtime);
    }

    private static int getIndirectionEfficient(Doc start, Doc search, int limit, DbConnector dbc) {
        int levelsFromThis = limit / 2 + limit % 2;
        int levelsFromOther = limit / 2 + 1;
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
        for (int thisIndex = 0; thisIndex < levelsFromThis + 1; thisIndex++) {
            for (int otherIndex = 0; otherIndex < levelsFromOther; otherIndex++) {
                if (otherIndex + thisIndex >= min) continue;
                List<DocId> fromDocs = fromThis.get(thisIndex);
                List<DocId> targetDocs = fromOther.get(otherIndex + 1);
                if (!Collections.disjoint(fromDocs, targetDocs)) {
                    min = thisIndex + otherIndex;
                }
            }
        }
        if (min == limit + 1) return -1;
        return min;
    }
}
