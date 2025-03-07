package Networking;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * UDPServerCS is a server implementation using UDP sockets. 
 * It listens for incoming packets, processes them, and maintains a list of active nodes.
 * 
 * @author Mischevious Mushroom Men
 */
public class UDPServerCS
{
    DatagramSocket socket = null;
    private static ConcurrentHashMap<String, Node> nodeMap = new ConcurrentHashMap<String, Node>();

    public UDPServerCS() 
    {

    }
    
    public void createAndListenSocket() {
        System.out.println("Server Successfully Booted\n~~Listening for connection~~");
            try {
                socket = new DatagramSocket(8001);
                while(true) {
                    byte[] incomingData = new byte[1024];
                    
                    DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                    socket.receive(incomingPacket);

                    //deserialize received packet
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(incomingPacket.getData(), 0, incomingPacket.getLength());
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    HACProtocol receivedObject = (HACProtocol)objectInputStream.readObject();

                    nodeMap.put(incomingPacket.getAddress().getHostAddress() + ":" + incomingPacket.getPort(), new Node(incomingPacket.getAddress().getHostAddress(), incomingPacket.getPort(), new ArrayList<File>(Arrays.asList(receivedObject.getLocalFiles()))));
                    nodeMap.get(incomingPacket.getAddress().getHostAddress() + ":" + incomingPacket.getPort()).heartbeatRecieved(); //Updates the node to have received it's heartbeat message

                    InetAddress IPAddress = incomingPacket.getAddress();
                    int port = incomingPacket.getPort();
                
                    Date time = new Date(System.currentTimeMillis());
                    System.out.println("Time Received: " + time + "\n" + nodeMap.get(incomingPacket.getAddress().getHostAddress() + ":" + incomingPacket.getPort()).toString());

                    //Packaging return HACProtocol
                    byte[] data;
                    try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); //initialize byteArrayOutputStream
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
                            Node[] tempArr = nodeMap.values().toArray(new Node[nodeMap.size()]);
                            HACProtocol sentObject = new HACProtocol("CS", 0, tempArr);

                            for(Node node:tempArr) {
                                System.out.print("Node: " + node.getID() + " Is Online?: " + !node.timeOut() + "\t");
                            }

                            try {
                                objectOutputStream.writeObject(sentObject);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            data = byteArrayOutputStream.toByteArray();
                        }
                        
                    DatagramPacket replyPacket = new DatagramPacket(data, data.length, IPAddress, port); //Sends the list of all nodes back to the pinging node
                        
                    socket.send(replyPacket);
                    Thread.sleep(2000);
                }
                // socket.close();
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
            UDPServerCS server = new UDPServerCS();
            server.createAndListenSocket();
        }
    }

    

