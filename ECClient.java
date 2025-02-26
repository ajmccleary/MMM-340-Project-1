import Networking.HACProtocol;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ECClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private final int SERVER_PORT = 9876;

    // public ECClient() {
    //     try {
    //         socket = new DatagramSocket();
    //         serverAddress = InetAddress.getByName("localhost"); // Change if needed
    //     } catch (SocketException | IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    public void sendPacket() {
        try {
            // Creating HACProtocol object
            String[] testArray = {"data1", "data2"};
            HACProtocol testProtocolPacket = new HACProtocol("ClientPacket", 1, 1, testArray);

            // Serialize HACProtocol object to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(testProtocolPacket);
            objectOutputStream.flush();

            byte[] serializedObject = byteArrayOutputStream.toByteArray();

            // Send object via UDP
            DatagramPacket sendPacket = new DatagramPacket(serializedObject, serializedObject.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);
            System.out.println("Packet sent to server.");

            // Prepare to receive response
            byte[] incomingData = new byte[1024];
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            socket.receive(incomingPacket);

            // Read response
            String response = new String(incomingPacket.getData(), 0, incomingPacket.getLength());
            System.out.println("Response from server: " + response);

            // Close resources
            objectOutputStream.close();
            byteArrayOutputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ECClient client = new ECClient();
        client.sendPacket();
    }
}
