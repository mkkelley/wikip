package wiki.doc;

import wiki.DbConnector;
import wiki.Savable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Michael Kelley on 4/30/14.
 * See LICENSE file for license information.
 */
public class Doc implements Savable {
    public final long id;
    public final String title;
    public final String text;
    private final static ExecutorService threadPool = Executors.newFixedThreadPool(8);

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

    public Doc(long id, String title, String text) {
        this.id = id;
        this.title = title;
        this.text = text;
    }

    public List<Doc> getLinkedDocs(DbConnector dbc) {
        return DocResource.getLinkedDocs(this, dbc);
    }

    public int parallelGetIndirection(Doc other, DbConnector dbc, int limit) {
        if (limit == -1) {
            return -1;
        }

        List<Doc> linkedDocs = getLinkedDocs(dbc);
        if (linkedDocs.contains(other)) {
            System.out.println(this.toString() + " links to " + other.toString());
            return 0;
        } else {
            ArrayList<Future<Integer>> tasks = new ArrayList<>(linkedDocs.size());
            for (Doc d : linkedDocs) {
                IndirectionCalculator ic = new IndirectionCalculator(d, other, dbc, limit - 1);
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
    public ArrayList<String> getTextLinks() {
        ArrayList<String> linkList = new ArrayList<>();

        byte[] bytes = text.getBytes();
        for (int i = 0; i < bytes.length - 1; i++) {
            if (bytes[i] == '[' && bytes[i+1] == '[') {
                for (int j = i + 2; j < bytes.length - 1; j++) {
                    if (bytes[j] == ']' && bytes[j+1] == ']') {
                        String s = new String(bytes, i + 2, j - i - 2);
                        s = s.split("\\|", -1)[0];
                        linkList.add(s);
                        i = j;
                        break;
                    }
                }
            }
        }
        return linkList;
    }

    @Override
    public void save(DbConnector dbc) {
        DocResource.save(this, dbc);
    }

    @Override
    public String toString() {
        return this.title;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Doc)) {
            return false;
        }
        Doc d = (Doc)o;
        return d.id == this.id;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }
}

