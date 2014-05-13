package wiki.doc;

import wiki.DbConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Michael Kelley on 5/13/14.
 * See LICENSE file for license information.
 */
public class ParallelIndirectionCalculator implements Callable<Integer> {
    private final static ExecutorService threadPool = Executors.newFixedThreadPool(8);

    private final Doc start;
    private final Doc search;
    private final DbConnector dbc;
    private final int limit;

    public ParallelIndirectionCalculator(Doc start, Doc search, DbConnector dbc, int limit) {
        this.start = start;
        this.search = search;
        this.dbc = dbc;
        this.limit = limit;
    }

    private synchronized Future<Integer> submitJob(Callable<Integer> call) {
        return threadPool.submit(call);
    }

    private synchronized void killThreadPool() throws InterruptedException {
        if (threadPool.isShutdown()) {
            return;
        }
        threadPool.shutdownNow();
        while (!threadPool.isShutdown()) {
            Thread.sleep(10);
        }
    }

    public int parallelGetIndirection() {
        if (limit == -1) {
            return -1;
        }

        List<Long> linkedDocs = start.getLinkedDocs(dbc);
        if (linkedDocs.contains(search.id)) {
            System.out.println(this.toString() + " links to " + search.toString());
            return 0;
        } else {
            ArrayList<Future<Integer>> tasks = new ArrayList<>(linkedDocs.size());
            for (Long l : linkedDocs) {
                Doc d = new Doc(l, "", "");
                IndirectionCalculator ic = new IndirectionCalculator(d, search, dbc, limit - 1);
                tasks.add(submitJob(ic));
            }
            System.out.println("All tasks submitted.");

            int minIndirection = -1;
            int n = 0;
            for (Future<Integer> result : tasks) {
                try {
                    int indirection = result.get();
                    if (indirection == 0) {
                        killThreadPool();
                        return 1;
                    }
                    if (indirection != -1 && (minIndirection == -1 || indirection < minIndirection)) {
                        minIndirection = indirection;
                        System.out.println("new minInd: " + minIndirection);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                n++;
                System.out.println("Done " + n + "/" + tasks.size());
            }
            System.out.println("All futures processed.");
            threadPool.shutdown();
            if (minIndirection == -1) return -1;
            else return minIndirection + 1;
        }
    }

    @Override
    public Integer call() throws Exception {
        return parallelGetIndirection();
    }
}
