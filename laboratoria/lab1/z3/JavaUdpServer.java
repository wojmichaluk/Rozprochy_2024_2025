import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.nio.ByteBuffer;

public class JavaUdpServer {
    public static void main(String args[]) {
        System.out.println("JAVA UDP SERVER");
        DatagramSocket socket = null;
        int portNumberServer = 9008;
        int portNumberClient = 9009;

        try {
            socket = new DatagramSocket(portNumberServer);
            byte[] receiveBuffer = new byte[1024];

            while(true) {
                Arrays.fill(receiveBuffer, (byte)0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                
                int nb = Integer.reverseBytes(ByteBuffer.wrap(receivePacket.getData()).getInt());
                System.out.println("received number: " + nb);

                InetAddress address = InetAddress.getByName("localhost");
                byte[] sendBuffer = ByteBuffer.allocate(4).putInt(Integer.reverseBytes(nb+1)).array();

                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, portNumberClient);
                socket.send(sendPacket);
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
