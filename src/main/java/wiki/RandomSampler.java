package wiki;

import wiki.doc.Doc;
import wiki.doc.EfficientIndirectionCalculator;
import wiki.doc.RandomDocGetter;
import wiki.result.Result;

import java.util.concurrent.*;

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

        final RandomDocGetter rdg = new RandomDocGetter(dbc);
        while (true) {
            try {
                calcParallelRandomIndirection(dbc, 500, rdg);
            } catch (Throwable e) {
                e.printStackTrace();
                System.gc();
                continue;
            }
        }
    }

    private final static ExecutorService threadPool = Executors.newFixedThreadPool(8);
    private static void calcParallelRandomIndirection(DbConnector dbc, int number, RandomDocGetter rdg) {
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
                result.save(dbc);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static void calculateRandomIndirection(DbConnector dbc, RandomDocGetter rdg) {
        calcParallelRandomIndirection(dbc, 1, rdg);
    }
}
