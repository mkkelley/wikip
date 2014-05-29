package wiki.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import wiki.DbConnector;
import wiki.result.Result;

import java.io.IOException;

/**
 * Created by Michael Kelley on 5/26/14.
 * See LICENSE file for license information.
 */
public class ResultConsumer {
    private final static String RESULT_QUEUE_NAME = "wiki_result_queue";

    public static void main(String[] args) throws IOException, InterruptedException {
        String rabbitMqUrl = null;
        String resultUrl = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("rs")) {
                resultUrl = args[i + 1];
            } else if (args[i].equals("rq")) {
                rabbitMqUrl = args[i + 1];
            }
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMqUrl);
        factory.setUsername("wiki");
        factory.setPassword("wiki");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(RESULT_QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages.");

        QueueingConsumer consumer = new QueueingConsumer(channel);
        boolean autoAck = false;
        channel.basicConsume(RESULT_QUEUE_NAME, autoAck, consumer);

        DbConnector dbc = new DbConnector(resultUrl);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(1000);
            if (delivery == null) break;
            String message = new String(delivery.getBody());
            System.out.println(" [x] Received '" + message + "'");
            saveResult(message, dbc);
            System.out.println(" [x] Done");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
        channel.close();
        connection.close();
    }

    private static void saveResult(String message, DbConnector dbc) {
        String[] resultArray = message.split(",");
        long startId = Long.valueOf(resultArray[0]);
        long searchId = Long.valueOf(resultArray[1]);
        int indirection = Integer.valueOf(resultArray[2]);
        int max = Integer.valueOf(resultArray[3]);
        long timeTaken = Long.valueOf(resultArray[4]);
        Result result = new Result(startId, searchId, indirection, max, timeTaken);
        result.save(dbc);
    }
}
