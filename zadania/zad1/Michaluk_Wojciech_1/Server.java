import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    public static void main(String[] args) throws IOException {
        System.out.println("JAVA SERVER");
        int portNumber = 12345;
        int currentId = 1;

        ServerSocket serverTcpSocket = null;
        DatagramSocket serverUdpSocket = null;
        HashMap<Integer, Socket> tcpClients = new HashMap<>();
        HashMap<Integer, Integer> udpClients = new HashMap<>();

        try {
            // create sockets
            serverTcpSocket = new ServerSocket(portNumber);
            serverUdpSocket = new DatagramSocket(portNumber);

            // thread pool
            ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();

            // single thread to handle udp clients
            executor.submit(new UdpHandler(serverUdpSocket, currentId, udpClients));

            while(true) {
                // accept tcp client
                Socket tcpSocket = serverTcpSocket.accept();
                System.out.println("client connected, client id: " + currentId);

                // send client its id
                PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
                out.println(currentId);

                // save tcp socket in a hashmap
                tcpClients.put(currentId, tcpSocket);

                // save port number for udp purposes
                udpClients.put(currentId, tcpSocket.getPort());
                
                // separate thread for each tcp connection
                executor.submit(new TcpClientHandler(tcpSocket, currentId++, tcpClients));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            // closing resources
            if (serverTcpSocket != null) {
                serverTcpSocket.close();
            }
            if (serverUdpSocket != null) {
                serverUdpSocket.close();
            }
        }
    }
}

class TcpClientHandler extends Thread {
    final Socket socket;
    final int id;
    final HashMap<Integer, Socket> tcpClients;

    TcpClientHandler(Socket socket, int id, HashMap<Integer, Socket> tcpClients) {
        this.socket = socket;
        this.id = id;
        this.tcpClients = tcpClients;
    }

    @Override
    public void run() {
        try {
            // in stream for a client
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {
                // read msg from a client 
                String msg = in.readLine();

                // check if exit msg
                if (msg.equals("EXIT")) {
                    System.out.println("client " + id + " exits!");

                    // so that client program terminates
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(msg);

                    // clear client info and resources
                    tcpClients.remove(id);
                    socket.close();

                    break;
                }

                // if not exit msg, display it and send to other clients
                System.out.println("client " + id + " sent a msg: " + msg);

                // finding other clients
                for (Integer client :tcpClients.keySet()) {
                    if (client != id) {
                        PrintWriter out = new PrintWriter(tcpClients.get(client).getOutputStream(), true);
                        out.println("msg from client " + id + ": " + msg);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class UdpHandler extends Thread {
    final DatagramSocket socket;
    int currentId;
    final HashMap<Integer, Integer> udpClients;

    UdpHandler(DatagramSocket socket, int currentId, HashMap<Integer, Integer> udpClients) {
        this.socket = socket;
        this.currentId = currentId;
        this.udpClients = udpClients;
    }

    @Override
    public void run() {
        byte[] receiveBuffer = new byte[1024];
        byte[] sendBuffer = new byte[1024];

        try {
            InetAddress address = InetAddress.getByName("localhost");

            while(true) {
                // prepare to receive msg
                Arrays.fill(receiveBuffer, (byte)0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String msg = new String(receivePacket.getData());

                // find sender client id
                int clientId = findClientId(receivePacket);

                // check if exit msg
                if (msg.substring(0, 4).equals("EXIT")) {
                    // so that client program terminates
                    sendBuffer = msg.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, udpClients.get(clientId));
                    socket.send(sendPacket);

                    // clear client info
                    udpClients.remove(clientId);
                    
                    continue;
                }

                // if not exit msg, display it and send to other clients
                System.out.println("client " + clientId + " sent a msg:\n" + msg);

                // finding other clients
                for (Integer client : udpClients.keySet()) {
                    if (client != clientId) {
                        int clientPort = udpClients.get(client);
                        sendBuffer = ("msg from client " + clientId + ":\n" + msg).getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, clientPort);
                        socket.send(sendPacket);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // helper function
    private int findClientId(DatagramPacket receivePacket) {
        int clientPort = receivePacket.getPort();

        for (Integer client : udpClients.keySet()) {
            if (udpClients.get(client) == clientPort) {
                return client;
            }
        }

        return 0;
    }
}
