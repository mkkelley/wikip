package wiki.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import wiki.DbConnector;
import wiki.doc.Doc;
import wiki.doc.RandomDocGetter;

import java.io.IOException;

/**
 * Created by Michael Kelley on 5/23/14.
 * See LICENSE file for license information.
 */
public class NewTask {
    private final static String QUEUE_NAME = "wiki_task_queue";

    public static void main(String[] args) throws IOException {
        int n = 500;
        String url = null;
        String rabbitMqUrl = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("ds")) {
                url = args[i + 1];
            } else if (args[i].equals("rq")) {
                rabbitMqUrl = args[i + 1];
            } else if (args[i].equals("n")) {
                n = Integer.valueOf(args[i + 1]);
            }
        }
        DbConnector ds = new DbConnector(url);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMqUrl);
        factory.setUsername("wiki");
        factory.setPassword("wiki");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        RandomDocGetter rdg = new RandomDocGetter(ds);
        for (int i = 0; i < n; i++) {
            String message = getMessage(rdg);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }

        channel.close();
        connection.close();
    }

    private static String getMessage(RandomDocGetter rdg) {
        Doc start = rdg.getRandomDoc();
        Doc search = rdg.getRandomDoc();
        return start.id + "," + search.id + "," + 6;
    }

    private static String joinStrings(String[] strings, String delimiter) {
        int length = strings.length;
        if (length == 0) return "";
        StringBuilder words = new StringBuilder(strings[0]);
        for (int i = 1; i < length; i++) {
            words.append(delimiter).append(strings[i]);
        }
        return words.toString();
    }
}
