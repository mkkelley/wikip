package wiki.messaging;

import com.rabbitmq.client.*;
import wiki.DbConnector;
import wiki.doc.*;
import wiki.result.Result;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Created by Michael Kelley on 5/23/14.
 * See LICENSE file for license information.
 */
public class Receive {
    private final static String QUEUE_NAME = "wiki_task_queue";
    private final static String RESULT_QUEUE_NAME = "wiki_result_queue";
    private static CompletionService<Result> threadPool;

    private static int jobs = 0;
    private static int maxJobs = 8;

    public static void main(String[] args) throws IOException, InterruptedException {
        String url = null;
        String rabbitMqUrl = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("ds")) {
                url = args[i + 1];
            } else if (args[i].equals("rq")) {
                rabbitMqUrl = args[i + 1];
            } else if (args[i].equals("mj")) {
                maxJobs = Integer.valueOf(args[i + 1]);
            }
        }
        if (rabbitMqUrl == null) {
            rabbitMqUrl = "192.168.1.108";
        }
        if (url == null) {
            url = "localhost";
        }
//        if (resultUrl == null) {
//            resultUrl = url;
//        }
        threadPool = new ExecutorCompletionService<>(Executors.newFixedThreadPool(maxJobs));
        System.out.println("DataSource: " + url);
//        System.out.println("ResultWrite: " + resultUrl);
        DbConnector ds = new DbConnector(url);
//        DbConnector rs = new DbConnector(resultUrl);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMqUrl);
        factory.setUsername("wiki");
        factory.setPassword("wiki");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(RESULT_QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages.");

        channel.basicQos(maxJobs);

        QueueingConsumer consumer = new QueueingConsumer(channel);
        boolean autoAck = false;
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [x] Received '" + message + "'");
            doWork(message, ds);
            saveResult(channel);
            System.out.println(" [x] Done");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }

    private static EfficientIndirectionCalculator decodeMessage(String message, DbConnector dataSource) {
        // message := [0-9],[0-9],[0-9]
        // message := startId, searchId, maxIndirection
        String[] params = message.split(",");
        if (params.length != 3) {
            throw new IllegalArgumentException("Message improperly formatted: " + message);
        }
        long startId = Long.valueOf(params[0]);
        long searchId = Long.valueOf(params[1]);
        int limit = Integer.valueOf(params[2]);
        Doc start = DocResource.getDoc(startId, dataSource);
        Doc search = DocResource.getDoc(searchId, dataSource);

        return new EfficientIndirectionCalculator(start, search, dataSource, limit);
    }

    private static String encodeResult(Result result) {
        return String.valueOf(result.startId)
                + ',' + result.searchId
                + ',' + result.indirection
                + ',' + result.max
                + ',' + result.time;
    }

    private static void saveResult(Channel results) throws IOException {
        if (jobs < maxJobs) {
            return;
        }
        try {
            Future<Result> result = threadPool.take();
            byte[] message = encodeResult(result.get()).getBytes();
            results.basicPublish("", RESULT_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message);
            System.out.println(" [x] Sent '" + new String(message) + "'");
//            result.get().save(results);
            jobs--;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static void doWork(String task, DbConnector dataSource) throws InterruptedException {
        EfficientIndirectionCalculator calculator = decodeMessage(task, dataSource);
        threadPool.submit(calculator);
        jobs++;
    }
}
