package Networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 
 * @author cjaiswal
 *
 *  
 * 
 */
public class UDPServerMC
{
    DatagramSocket socket = null;
    private static ConcurrentHashMap<String, Node> nodeMap = new ConcurrentHashMap<String, Node>();
    //private static ArrayList<Node>[] fileList = new ArrayList<Node>[];

    public UDPServerMC() 
    {

    }
    
    public void createAndListenSocket() {
        System.out.println("Server Successfully Booted\n~~Listening for connection~~");
        try {
            socket = new DatagramSocket(8001);
            byte[] incomingData = new byte[1024];

            int c = 0;

            while (c < 3) 
            {
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);

                //deserialize received packet
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(incomingPacket.getData(), 0, incomingPacket.getLength());
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                HACProtocol receivedObject = (HACProtocol) objectInputStream.readObject();
                nodeMap.put(incomingPacket.getAddress().getHostAddress() + ":" + incomingPacket.getPort(), new Node(incomingPacket.getAddress().getHostAddress(), incomingPacket.getPort(), new ArrayList<File>(Arrays.asList(receivedObject.getLocalFiles()))));
                nodeMap.get(incomingPacket.getAddress().getHostAddress() + ":" + incomingPacket.getPort()).heartbeatRecieved();


                InetAddress IPAddress = incomingPacket.getAddress();
                int port = incomingPacket.getPort();
                String message = nodeMap.get(IPAddress.getHostAddress()).getFileNames(); //version used for now, would actually be data
                
                System.out.println("Received message from client: " + message);
                System.out.println("Client IP:"+IPAddress.getHostAddress());
                System.out.println("Client port:"+port);
                
                String reply = "Thank you for the message";

                //Packaging return HACProtocol
                byte[] data;
                try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); //initialize byteArrayOutputStream
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
                        Node[] tempArr = nodeMap.values().toArray(new Node[nodeMap.size()]);
                        HACProtocol sentObject = new HACProtocol("CS", 0, tempArr);

                        try {
                            objectOutputStream.writeObject(sentObject);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        data = byteArrayOutputStream.toByteArray();
                    }
                // byte[] data = reply.getBytes();
                
                DatagramPacket replyPacket = new DatagramPacket(data, data.length, IPAddress, port); //Sends the list of all nodes back to the pinging node
                
                socket.send(replyPacket);
                Thread.sleep(2000);

                c++;
            }

            socket.close();
        } 
        catch (SocketException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException i) 
        {
            i.printStackTrace();
        } 
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        } catch (ClassNotFoundException e) 
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) 
    {
        UDPServerMC server = new UDPServerMC();
        server.createAndListenSocket();
    }
}

