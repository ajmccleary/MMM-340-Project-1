
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ECServer {
    private DatagramSocket socket;
    private final ExecutorService executorService;

    public ECServer() {
        executorService = Executors.newFixedThreadPool(10); // Create a thread pool with 10 threads
    }

    public void createAndListenSocket() {
        try {
            socket = new DatagramSocket(9876);
            byte[] incomingData = new byte[1024];

            while (true) {
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);
                executorService.execute(new ClientHandler(incomingPacket, socket)); // Submit the incoming packet to the executor
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Close the socket when done
            }
        }
    }

    public static void main(String[] args) {
        ECServer server = new ECServer();
        server.createAndListenSocket();
    }
}

class ClientHandler implements Runnable {
    private final DatagramPacket packet;
    private final DatagramSocket socket;

    public ClientHandler(DatagramPacket packet, DatagramSocket socket) {
        this.packet = packet;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            String message = new String(packet.getData(), 0, packet.getLength());
            InetAddress IPAddress = packet.getAddress();
            int port = packet.getPort();

            System.out.println("Received message from client: " + message);
            System.out.println("Client IP: " + IPAddress.getHostAddress());
            System.out.println("Client port: " + port);

            String reply = "Thank you for the message";
            byte[] data = reply.getBytes();
            DatagramPacket replyPacket = new DatagramPacket(data, data.length, IPAddress, port);
            socket.send(replyPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}