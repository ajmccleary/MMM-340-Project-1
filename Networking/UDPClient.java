package Networking;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
/**
 * 
 * @author cjaiswal
 *
 *  
 * 
 */
public class UDPClient //MODIFIED TO USE SERIALIZABLE DIRECT OBJECT SEND VIA OBJECTOUTPUTSTREAM, TEST WITH PROTOCOLTESTSERVER.JAVA
{
    DatagramSocket Socket;

    public UDPClient() 
    {

    }

    public void createAndListenSocket() 
    {
        try 
        {
            // Socket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] incomingData = new byte[1024];
            String sentence = "Viehmann";
            byte[] data = sentence.getBytes();

            String testArray [] = {""};

            HACProtocol testProtocolPacket = new HACProtocol(data, IPAddress, 12345, "doesn'tmatter", 0, 1, 1, testArray);

            // DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
            // Socket.send(sendPacket);

        try (Socket socket = new Socket("localhost", 12345)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(testProtocolPacket);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

            System.out.println("Message sent from client");
            // DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            // Socket.receive(incomingPacket);
            // String response = new String(incomingPacket.getData());
            // System.out.println("Response from server:" + response);
            // Socket.close();
        }
        catch (UnknownHostException e) 
        {
            e.printStackTrace();
        } 
        // catch (SocketException e) 
        // {
        //     e.printStackTrace();
        // } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) 
    {
        UDPClient client = new UDPClient();
        client.createAndListenSocket();
    }
}

