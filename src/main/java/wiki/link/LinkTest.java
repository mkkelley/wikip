package wiki.link;

import wiki.DbConnector;
import wiki.doc.Doc;
import wiki.doc.DocResource;
import wiki.doc.ParallelIndirectionCalculator;

/**
 * Created by Michael Kelley on 5/2/14.
 * See LICENSE file for license information.
 */
public class LinkTest {
    public static void main(String[] args) throws Exception {
        // args[0] - database jdbc url
        // args[1] - fromDoc
        // args[2] - toDoc
        String url;
        long to;
        long from;
        if (args.length < 1) {
            url = "localhost";
        } else {
            url = args[0];
        }
        if (args.length < 3) {
            from = 14279;
            to = 3016667;
//            to = 6886;
        } else {
            from = Long.parseLong(args[1]);
            to = Long.parseLong(args[2]);
        }
        DbConnector dbc = new DbConnector(url);
        Doc d = DocResource.getDoc(from, dbc);
        Doc other = DocResource.getDoc(to, dbc);
        long milis = System.currentTimeMillis();
        int ind = new ParallelIndirectionCalculator(d, other, dbc, 3).call();
//        int ind = d.getIndirectionEfficient(other, dbc, 3);
        System.out.println(ind);
        System.out.println(System.currentTimeMillis() - milis);

    }
}
