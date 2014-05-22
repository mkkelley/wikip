package wiki;

import wiki.doc.Doc;
import wiki.doc.EfficientIndirectionCalculator;
import wiki.doc.RandomDocGetter;
import wiki.result.Result;

import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Created by Michael Kelley on 5/4/14.
 * See LICENSE file for license information.
 */
public class RandomSampler {
    public static void main(String[] args) {
        String url = null;
        String resultUrl = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("ds")) {
                url = args[i + 1];
            } else if (args[i].equals("rs")) {
                resultUrl = args[i + 1];
            }
        }
        if (url == null) {
            url = "localhost";
        }
        if (resultUrl == null) {
            resultUrl = url;
        }
        System.out.println("DataSource: " + url);
        System.out.println("ResultWrite: " + resultUrl);
//        System.out.print("Continue? ");
//        Scanner sc = new Scanner(System.in);
//        String answer = sc.nextLine();
//        if (!answer.equals("Y")) {
//            return;
//        }
        DbConnector dbc = new DbConnector(url);
        DbConnector resultDbc = new DbConnector(resultUrl);

        final RandomDocGetter rdg = new RandomDocGetter(dbc);
        while (true) {
            try {
                calcParallelRandomIndirection(dbc, 500, rdg, resultDbc);
            } catch (Throwable e) {
                e.printStackTrace();
                System.gc();
                continue;
            }
        }
    }

    private final static ExecutorService threadPool = Executors.newFixedThreadPool(8);
    private static void calcParallelRandomIndirection(DbConnector dbc, int number, RandomDocGetter rdg, DbConnector results) {
        CompletionService<Result> cs = new ExecutorCompletionService<>(threadPool);
        for (int i = 0; i < number; i++) {
            Doc fromDoc = rdg.getRandomDoc();
            Doc toDoc = rdg.getRandomDoc();

            EfficientIndirectionCalculator eic = new EfficientIndirectionCalculator(fromDoc, toDoc, dbc, 6);
            cs.submit(eic);
        }
        for (int i = 0; i < 500; i++) {
            try {
                Result result = cs.take().get();
                result.save(results);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static void calculateRandomIndirection(DbConnector dbc, RandomDocGetter rdg) {
        calcParallelRandomIndirection(dbc, 1, rdg, dbc);
    }
}
