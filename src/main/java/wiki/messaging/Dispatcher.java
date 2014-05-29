package wiki.messaging;

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
            default:
                System.out.println("newTask ds rq n or receive ds rq mj or resultConsumer rs rq");
                return;
        }
    }
}
