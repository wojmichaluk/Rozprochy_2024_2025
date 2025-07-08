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

public class Admin {
    public static void main(String[] args) throws Exception {
        // check if no arguments are given - we don't want any
        if (args.length > 0) {
            System.out.println("No arguments expected, but received some. Exitting.");
            return;
        }

        // info
        System.out.println("-".repeat(20) + "ADMIN" + "-".repeat(20));
        System.out.println("List of available commands:");
        System.out.println("-'SQU' to send message to all squads,");
        System.out.println("-'SUP' to send message to all suppliers,");
        System.out.println("-'BOTH' to send message to all squads and all suppliers,");
        System.out.println("-'X' to exit.\n");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();        

        // declare exchange, type topic
        String EXCHANGE_TOPIC = "exchange_topic";
        channel.exchangeDeclare(EXCHANGE_TOPIC, BuiltinExchangeType.TOPIC);

        // declare queue & bind it
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_TOPIC, "admin.#");

        // handling all messages in the system
        Consumer allMessagesHandler = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String name = new String(body, "UTF-8");

                // present in both cases
                String serviceType = properties.getHeaders().get("serviceType").toString();

                // determine who sent the message
                String sender = properties.getHeaders().get("sender").toString();

                if ("squad".equals(sender)) {
                    System.out.println("SYSTEM: Squad '" + name + "' placed an order for '" + serviceType + "'\n");
                } else if ("supplier".equals(sender)) {
                    String orderNo = properties.getHeaders().get("orderNo").toString();
                    System.out.println("SYSTEM: Supplier '" + name + "' fulfilled an order for '" + serviceType + "', order number: " + orderNo + "\n");
                } else {
                    System.out.println("SYSTEM: Sender of the message not recognized.\n");
                }
            }
        };

        // start listening to all messages
        channel.basicConsume(queueName, true, allMessagesHandler);

        while (true) {
            // read command
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String command = br.readLine();

            // terminate the program if user wants to
            if ("X".equals(command)) {
                System.out.println("Goodbye!");
                break;
            }

            // prepare to send message
            boolean flag = false;
            String key = "";
            HashMap<String, Object> headers = new HashMap<String, Object>();

            switch (command) {
                case "SQU":
                    headers.put("messageType", "all squads");
                    key = "squad";
                    flag = true;
                    break;
                case "SUP":
                    headers.put("messageType", "all suppliers");
                    key = "supplier";
                    flag = true;
                    break;
                case "BOTH":
                    headers.put("messageType", "all squads and all suppliers");
                    key = "squad.supplier";
                    flag = true;
                    break;
                default:
                    System.out.println("Command '" + command + "' not recognized. Please try again.\n");
                    break;
            }

            // proceed only if valid command
            if (flag) {
                // read message
                System.out.print("Enter message: ");
                String message = br.readLine();
                System.out.println();

                // send message
                channel.basicPublish(
                    EXCHANGE_TOPIC, 
                    key, 
                    new AMQP.BasicProperties.Builder()
                        .headers(headers)
                        .build(),
                    message.getBytes("UTF-8")
                );
            }
        }

        // close channel and connection
        channel.close();
        connection.close();
    }
}
