package wiki;

import wiki.doc.Doc;
import wiki.doc.EfficientIndirectionCalculator;
import wiki.doc.RandomDocGetter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Michael Kelley on 5/4/14.
 * See LICENSE file for license information.
 */
public class RandomSampler {
    public static void main(String[] args) {
        String url;
        if (args.length < 1) {
            url = "localhost";
        } else {
            url = args[0];
        }
        DbConnector dbc = new DbConnector(url);
        long n;
        List<Long> count = dbc.jdbcTemplate.query("SELECT COUNT(*) AS total FROM pages",
                (resultSet, i) -> resultSet.getLong("total"));
        n = count.get(0);

        final RandomDocGetter rdg = new RandomDocGetter(dbc);
        while (true) {
//            calculateRandomIndirection(dbc, n);
            calcParallelRandomIndirection(dbc, n, 500, rdg);
        }
    }

    private final static ExecutorService threadPool = Executors.newFixedThreadPool(8);
    private static void calcParallelRandomIndirection(DbConnector dbc, long n, int number, RandomDocGetter rdg) {
        List<Future<Integer>> futureList = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            Doc fromDoc = rdg.getRandomDoc();
            Doc toDoc = rdg.getRandomDoc();

            EfficientIndirectionCalculator eic = new EfficientIndirectionCalculator(fromDoc, toDoc, dbc, 6);
            Future<Integer> future = threadPool.submit(eic);
            futureList.add(future);
        }
        for (Future<Integer> fi : futureList) {
            try {
                fi.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
//
//    private static int calculateRandomIndirection(DbConnector dbc, long n) {
//        Doc fromDoc = getRandomDoc(dbc, n);
//        Doc toDoc = getRandomDoc(dbc, n);
//        System.out.println("Getting indirection between " + fromDoc + " and " + toDoc);
//
//        long millis = System.currentTimeMillis();
//        int indirection = fromDoc.getIndirectionEfficient(toDoc, dbc, 6);
//        System.out.println("Indirection of " + indirection + " from " + fromDoc + " to " + toDoc + " took " + (System.currentTimeMillis() - millis));
//        return indirection;
//    }

    private static Doc getRandomDoc(DbConnector dbc, long n) {
        Doc randDoc = null;
        do {
            List<Doc> doc = dbc.jdbcTemplate.query("select * from pages offset random() * ? limit 1 ;", new Object[]{n},
                    (resultSet, i) -> {
                        long id = resultSet.getLong(1);
                        String title = resultSet.getString(2);
                        return new Doc(id, title, "");
                    });
            if (!doc.isEmpty()) {
                randDoc = doc.get(0);
            }
        } while (randDoc == null);
        return randDoc;
    }
}
