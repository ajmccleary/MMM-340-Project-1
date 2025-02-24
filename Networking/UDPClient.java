package Networking;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.*;
/**
 * 
 * @author cjaiswal
 *
 *  
 * 
 */
public class UDPClient 
{
    DatagramSocket Socket;
    MyFileReader fileRec; 

    public UDPClient() 
    {

    }

    public void createAndListenSocket() 
    {
        try 
        {
            Socket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] incomingData = new byte[1024];
            String sentence = "Viehmann";
            byte[] data = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
            Socket.send(sendPacket);
            System.out.println("Message sent from client");
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            Socket.receive(incomingPacket);
            //50/50 Broken ass code
            ByteArrayInputStream bis = new ByteArrayInputStream(incomingPacket.getData());

            try (ObjectInput in = new ObjectInputStream(bis)) {
                fileRec = (MyFileReader)in.readObject(); 
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            
            String response = new String(fileRec.getFiles().toString());
            //50/50 Broken ass code
            
            
            System.out.println("Response from server:" + response);
            Socket.close();
        }
        catch (UnknownHostException e) 
        {
            e.printStackTrace();
        } 
        catch (SocketException e) 
        {
            e.printStackTrace();
        } 
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

