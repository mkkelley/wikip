package wiki.messaging;

import wiki.doc.DocImporter;
import wiki.link.LinkImporter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Michael Kelley on 5/26/14.
 * See LICENSE file for license information.
 */
public class Dispatcher {
    public static void main(String[] args) throws IOException, InterruptedException {
        String[] dispatchedArgs = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
            case "newTask":
                NewTask.main(dispatchedArgs);
                break;
            case "receive":
                Receive.main(dispatchedArgs);
                break;
            case "resultConsumer":
                ResultConsumer.main(dispatchedArgs);
                break;
            case "docImporter":
                DocImporter.main(dispatchedArgs);
                break;
            case "linkImporter":
                LinkImporter.main(dispatchedArgs);
                break;
            case "treeceive":
                Treeceive.main(dispatchedArgs);
                break;
            case "treeNewTask":
                NewTreeTask.main(dispatchedArgs);
                break;
            default:
                System.out.println(
                        "newtask ds <> rq <> n <>\n" +
                        "receive ds <> rq <> mj <>\n" +
                        "resultConsumer rs <> rq <>\n" +
                        "docImporter path <>\n" +
                        "linkImporter path <>\n" +
                        "treeceive ds <> rq <> mj <>\n" +
                        "treeNewTask ds <> rq <> n <>");
                return;
        }
    }
}
