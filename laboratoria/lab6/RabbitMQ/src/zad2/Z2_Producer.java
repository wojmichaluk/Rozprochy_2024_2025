package zad2;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Z2_Producer {
    public static void main(String[] args) throws Exception {
        // info
        System.out.println("Z2 PRODUCER");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        String EXCHANGE_NAME = "exchange1";

        // exchange: direct
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // exchange: topic
        // channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        while (true) {
            // read msg
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter routing key: ");
            String key = br.readLine();
            System.out.print("Enter message: ");
            String message = br.readLine();

            // break condition
            if ("exit".equals(message)) {
                break;
            }

            // publish
            channel.basicPublish(EXCHANGE_NAME, key, null, message.getBytes("UTF-8"));
            System.out.println("Sent: " + message + "\n");
        }
    }
}
