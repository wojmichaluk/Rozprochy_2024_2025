package zad1;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;

public class Z1a_Consumer {
    public static void main(String[] args) throws Exception {
        // info
        System.out.println("Z1 CONSUMER");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // queue
        String QUEUE_NAME = "queue1";
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // consumer (handle msg)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                System.out.println("Processing message...");

                try {
                    int timeToSleep = Integer.parseInt(message); 
                    Thread.sleep(timeToSleep * 1000); 
                } catch (InterruptedException ie) {
                    System.out.println(ie.getMessage());
                } catch (NumberFormatException nfe) {
                    System.out.println("Not a number, but number is expected!");
                }

                // ack after processing msg
                channel.basicAck(envelope.getDeliveryTag(), false);

                System.out.println("Processed.\n");
            }
        };

        // start listening
        System.out.println("Waiting for messages...");

        // ack after receiving msg
        // channel.basicConsume(QUEUE_NAME, true, consumer);

        // no ack at all / ack after processing msg
        channel.basicConsume(QUEUE_NAME, false, consumer); 

        // close
        // channel.close();
        // connection.close();
    }
}
