import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Squad {
    // squad's name
    private static String name;

    // echanges names
    private static String EXCHANGE_DIRECT = "exchange_direct";
    private static String EXCHANGE_TOPIC = "exchange_topic";

    public static void main(String[] args) throws Exception {
        // check if only name is given
        if (args.length != 1) {
            System.out.println("Failed to provide squad's name and nothing more. Exitting.");
            return;
        }

        // get squad's name
        name = args[0];

        // info
        System.out.println("-".repeat(20) + "SQUAD" + "-".repeat(20));
        System.out.println("Name: " + name);
        System.out.println("Orders: 'O' for oxygen, 'S' for shoes, 'B' for backpack, 'X' to exit.");
        System.out.println("Place your orders one at a time.\n");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // declare exchanges, type direct & fanout
        channel.exchangeDeclare(EXCHANGE_DIRECT, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(EXCHANGE_TOPIC, BuiltinExchangeType.TOPIC);

        // declare queues & bind it
        String QUEUE_NAME = name + "_queue";
        String QUEUE_NAME_ADMIN = name + "_queue_admin";

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(QUEUE_NAME_ADMIN, false, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_DIRECT, name);
        channel.queueBind(QUEUE_NAME_ADMIN, EXCHANGE_TOPIC, "squad.#");

        // handling confirmation messages
        Consumer confirmationHandler = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String supplierName = new String(body, "UTF-8");
                
                // process order confirmation
                String orderNo = properties.getHeaders().get("orderNo").toString();
                String orderType = properties.getHeaders().get("serviceType").toString();

                // ack after processing msg
                channel.basicAck(envelope.getDeliveryTag(), false);

                System.out.println("Order for: '" + orderType + "', number: " + orderNo + " fulfilled by: '" + supplierName + "'\n");
            }
        };

        // handling admin messages
        Consumer adminMessagesHandler = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");

                // get message type
                String messageType = properties.getHeaders().get("messageType").toString();
                
                System.out.println("MESSAGE FOR " + messageType.toUpperCase());
                System.out.println("!!! Message from admin: '" + message + "' !!!\n");
            }
        };

        // start listening to confirmations from suppliers
        channel.basicConsume(QUEUE_NAME, false, confirmationHandler);

        // start listening to messages from admin
        channel.basicConsume(QUEUE_NAME_ADMIN, true, adminMessagesHandler);

        // orders dict
        HashMap<String, String> dict = new HashMap<>();
        dict.put("O", "oxygen");
        dict.put("S", "shoes");
        dict.put("B", "backpack");

        while (true) {
            // read order
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String order = br.readLine();

            // terminate the program if user wants to
            if ("X".equals(order)) {
                System.out.println("Goodbye!");
                break;
            }

            if (!dict.containsKey(order)) {
                System.out.println("Order '" + order + "' not recognized. Please try again.\n");
            } else {
                String key = dict.get(order);

                // send request to suppliers & copy to admin
                HashMap<String, Object> headers = new HashMap<>();
                headers.put("sender", "squad");
                headers.put("serviceType", key);

                channel.basicPublish(
                    EXCHANGE_DIRECT, 
                    key, 
                    new AMQP.BasicProperties.Builder()
                        .headers(headers)
                        .build(), 
                    name.getBytes("UTF-8")
                );

                channel.basicPublish(
                    EXCHANGE_TOPIC, 
                    "admin", 
                    new AMQP.BasicProperties.Builder()
                        .headers(headers)
                        .build(), 
                    name.getBytes("UTF-8")
                );
                
                System.out.println("Sent request for '" + key + "'");
            }
        }

        // close channel and connection
        channel.close();
        connection.close();
    }
}
