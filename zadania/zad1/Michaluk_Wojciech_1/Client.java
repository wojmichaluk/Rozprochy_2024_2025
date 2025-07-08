import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    // to save client id from server
    static int id;

    public static void main(String[] args) throws IOException {
        System.out.println("JAVA CLIENT");
        String hostName = "localhost";
        int serverPortNumber = 12345;
        int multicastPortNumber = 23456;

        Socket tcpSocket = null;
        DatagramSocket udpSocket = null;
        MulticastSocket multiSocket = null;

        try {
            // create sockets
            tcpSocket = new Socket(hostName, serverPortNumber);
            multiSocket = new MulticastSocket(multicastPortNumber);

            // set the same port number as in tcp socket
            udpSocket = new DatagramSocket(tcpSocket.getLocalPort());

            // in stream for tcp socket
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

            // create a thread for reading input and sending messages
            Thread readInputThread = new Thread(new ReadInputHandler(tcpSocket, udpSocket, multiSocket, serverPortNumber, multicastPortNumber));
            readInputThread.start();

            // create a thread for receiving udp msgs 
            Thread udpThread = new Thread(new UdpHandler(udpSocket));
            udpThread.start();

            // create a thread for receiving multicast msgs
            Thread multicastThread = new Thread(new MulticastHandler(multiSocket));
            multicastThread.start();

            // main thread for receiving tcp msgs
            // first message contains client id, useful after exit
            id = Integer.valueOf(in.readLine());

            while (true) {
                String msg = in.readLine();

                // if client wanted to exit
                if (msg.equals("EXIT")) {
                    tcpSocket.close();
                    break;
                }

                // if received regular tcp msg
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ReadInputHandler extends Thread {
    final Socket tcpSocket;
    final DatagramSocket udpSocket;
    final MulticastSocket multiSocket;
    final int serverPortNumber;
    final int multicastPortNumber;

    ReadInputHandler(Socket tcpSocket, DatagramSocket udpSocket, MulticastSocket multiSocket, int serverPortNumber, int multicastPortNumber) {
        this.tcpSocket = tcpSocket;
        this.udpSocket = udpSocket;
        this.multiSocket = multiSocket;
        this.serverPortNumber = serverPortNumber;
        this.multicastPortNumber = multicastPortNumber;
    }

    @Override
    public void run() {
        // to read input from a client
        Scanner scanner = new Scanner(System.in);

        // guitar ASCII art
        String guitarAscii = "   _______       __\n"
                .concat(" /   ------.   / ._`_\n")
                .concat("|  /         ~--~    \\\n")
                .concat("| |             __    `.____________________ _^-----^\n")
                .concat("| |  I=|=======/--\\=========================| o o o |\n")
                .concat("\\ |  I=|=======\\__/=========================|_o_o_o_|\n")
                .concat(" \\|                   /                       ~    ~\n")
                .concat("   \\       .---.    .\n")
                .concat("     -----'     ~~''\n");

        try {
            InetAddress udpAddress = InetAddress.getByName("localhost");
            InetAddress multiAddress = InetAddress.getByName("230.0.0.0");
            SocketAddress multiSocketAddress = new InetSocketAddress(multiAddress, multicastPortNumber);

            while (true) {
                // read input
                String msg = scanner.nextLine();

                // check if udp, multicast or exit msg
                if (msg.equals("U")) {
                    byte[] sendBuffer = guitarAscii.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, udpAddress, serverPortNumber);
                    udpSocket.send(sendPacket);
                } else if (msg.equals("M")) {
                    byte[] sendBuffer = guitarAscii.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, multiAddress, multicastPortNumber);

                    // so that client doesn't receive its own msg
                    multiSocket.leaveGroup(multiSocketAddress, null);
                    udpSocket.send(sendPacket);
                    multiSocket.joinGroup(multiSocketAddress, null);
                } else if (msg.equals("EXIT")) {
                    // inform server via tcp
                    PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
                    out.println(msg);

                    // multicast doesn't involve server
                    byte[] sendBuffer = ("EXIT" + Client.id).getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, multiAddress, multicastPortNumber);
                    udpSocket.send(sendPacket);

                    // inform server via udp
                    sendBuffer = msg.getBytes();
                    sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, udpAddress, serverPortNumber);
                    udpSocket.send(sendPacket);

                    break;
                } else {
                    // send regular msg via TCP
                    PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
                    out.println(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}

class UdpHandler extends Thread {
    final DatagramSocket udpSocket;

    UdpHandler(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
    }

    @Override
    public void run() {
        byte[] receiveBuffer = new byte[1024];

        try {
            while(true) {
                Arrays.fill(receiveBuffer, (byte)0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                udpSocket.receive(receivePacket);

                String msg = new String(receivePacket.getData());

                // if client wanted to exit
                if (msg.substring(0, 4).equals("EXIT")) {
                    udpSocket.close();
                    break;
                }

                // regular msg
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MulticastHandler extends Thread {
    final MulticastSocket multicastSocket;

    MulticastHandler(MulticastSocket multicastSocket) {
        this.multicastSocket = multicastSocket;
    }

    @Override
    public void run() {
        byte[] receiveBuffer = new byte[1024];

        try {
            InetAddress group = InetAddress.getByName("230.0.0.0");
            SocketAddress multiSocketAddress = new InetSocketAddress(group, multicastSocket.getLocalPort());
            multicastSocket.joinGroup(multiSocketAddress, null);

            while (true) {
                Arrays.fill(receiveBuffer, (byte)0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                multicastSocket.receive(receivePacket);

                String msg = new String(receivePacket.getData());

                // if particular client from a group wanted to exit
                if (msg.substring(0, 4).equals("EXIT")) {
                    if (IdExtracter.extractId(msg, 4) == Client.id) {
                        multicastSocket.close();
                        break;
                    }

                    // do nothing if it's not that client
                    continue;
                }

                // regular msg
                System.out.println("wiadomosc grupowa miedzy klientami:");
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class IdExtracter {
    static public int extractId(String msg, int start) {
        int id = 0;
        int index = start;
    
        while (Character.isDigit(msg.charAt(index))) {
            id = 10 * id + Integer.valueOf(msg.charAt(index++) - '0');
        }
    
        return id;
    }
}
