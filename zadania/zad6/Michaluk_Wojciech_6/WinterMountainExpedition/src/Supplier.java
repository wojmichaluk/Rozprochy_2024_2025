import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class Supplier {
    // supplier's name
    private static String name;

    // queues names
    private static String QUEUE_OXYGEN = "queue_oxygen";
    private static String QUEUE_SHOES = "queue_shoes";
    private static String QUEUE_BACKPACK = "queue_backpack";

    // exchange name
    private static String EXCHANGE_DIRECT = "exchange_direct";
    private static String EXCHANGE_TOPIC = "exchange_topic";

    public static void main(String[] args) throws Exception {
        // check if at least name is given
        if (args.length < 1) {
            System.out.println("Failed to provide supplier's name. Exitting.");
            return;
        }

        // get supplier's name
        name = args[0];

        // info
        System.out.println("-".repeat(20) + "SUPPLIER" + "-".repeat(20));
        System.out.println("Name: " + name);

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();        

        // declare exchanges, type direct & topic
        channel.exchangeDeclare(EXCHANGE_DIRECT, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(EXCHANGE_TOPIC, BuiltinExchangeType.TOPIC);

        // declare queue & bind it
        String QUEUE_NAME_ADMIN = name + "_queue_admin";
        channel.queueDeclare(QUEUE_NAME_ADMIN, false, false, false, null);
        channel.queueBind(QUEUE_NAME_ADMIN, EXCHANGE_TOPIC, "#.supplier");

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

        // services provided by supplier
        HashSet<String> services = new HashSet<>();

        for (int i = 1; i < args.length; i++) {
            services.add(args[i]);
        }

        // declare queues & bind them based on provided services
        if (services.contains("oxygen")) {
            channel.queueDeclare(QUEUE_OXYGEN, false, false, false, null);
            channel.queueBind(QUEUE_OXYGEN, EXCHANGE_DIRECT, "oxygen");

            // handling orders
            handleRequest(QUEUE_OXYGEN, channel);
        }

        if (services.contains("shoes")) {
            channel.queueDeclare(QUEUE_SHOES, false, false, false, null);
            channel.queueBind(QUEUE_SHOES, EXCHANGE_DIRECT, "shoes");

            // handling orders
            handleRequest(QUEUE_SHOES, channel);
        }

        if (services.contains("backpack")) {
            channel.queueDeclare(QUEUE_BACKPACK, false, false, false, null);
            channel.queueBind(QUEUE_BACKPACK, EXCHANGE_DIRECT, "backpack");

            // handling orders
            handleRequest(QUEUE_BACKPACK, channel);
        }

        // load balancing
        channel.basicQos(1);

        // start listening to orders
        System.out.println("Waiting for orders...\n");

        // start listening to messages from admin
        channel.basicConsume(QUEUE_NAME_ADMIN, true, adminMessagesHandler);
    }

    private static void handleRequest(String queueName, Channel channel) throws IOException {
        Consumer requestHandler = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String squadName = new String(body, "UTF-8");
                String serviceType = properties.getHeaders().get("serviceType").toString();
                System.out.println("Received order for: '" + serviceType + "' from: '" + squadName + "'");

                // process order
                System.out.println("Processing order...");

                // order number based on time
                long orderNo = System.currentTimeMillis();
                System.out.println("Assigned order number: " + orderNo);

                // ack after processing msg
                channel.basicAck(envelope.getDeliveryTag(), false);

                // add orderNo to headers
                HashMap<String, Object> headers = new HashMap<String, Object>();
                headers.put("orderNo", orderNo);
                headers.put("serviceType", serviceType);
                headers.put("sender", "supplier");

                // send confirmation
                channel.basicPublish(
                    EXCHANGE_DIRECT, 
                    squadName, 
                    new AMQP.BasicProperties.Builder()
                        .headers(headers)
                        .build(), 
                    name.getBytes("UTF-8")
                );

                // send copy to admin
                channel.basicPublish(
                    EXCHANGE_TOPIC, 
                    "admin", 
                    new AMQP.BasicProperties.Builder()
                        .headers(headers)
                        .build(), 
                    name.getBytes("UTF-8")
                );

                System.out.println("Order processed and sent\n");
            }
        };

        // start listening to orders
        channel.basicConsume(queueName, false, requestHandler);
    }
}
