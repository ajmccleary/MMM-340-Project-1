package Networking;

import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

import UIElements.Node;

/**
 * 
 * @author cjaiswal
 *
 *  
 * 
 */
public class UDPClient {
    //instance variables
    private static DatagramSocket Socket;
    private static ExecutorService executorService;
    private static int nodeNum; //0-6, correlate to line of ipconfig to assign socket to
    private static int portNum;
    private static InetAddress ipAddress;
    private static int sequenceNum = 0;
    private static ConcurrentHashMap<String,Node> nodeMap = new ConcurrentHashMap<String,Node>();

    //initialize scanner variables
	private static File inFile = new File("Networking\\ipConfig.txt");
    private static String nextLine;

    public UDPClient() {
        try (Scanner fileInput = new Scanner(inFile)) { //initialize scanner
            //initialize count variable
            int count = 0;

            //scan through file
            do {
                //store next line input
                nextLine = fileInput.nextLine();

                //parse input and store in new node
                Node newNode = new Node(nextLine.split(" ")[0], Integer.parseInt(nextLine.split(" ")[1]), new ArrayList<File>());

                //check if count matches assigned client node num
                if (count == UDPClient.nodeNum) {
                    newNode.setToSelf();
                    UDPClient.ipAddress = InetAddress.getByName(newNode.getIP());
                    UDPClient.portNum = newNode.getPort();
                }

                //check if newNode is NOT node representing this computer
                if (!newNode.isNodeSelf()) {
                    //add node to hash map (key is id)
                    nodeMap.put(newNode.getID(), newNode);
                }

                //increment count
                count++;
            } while (fileInput.hasNextLine());

            //close scanner
            fileInput.close();

        } catch (FileNotFoundException e) { //catch potential error thrown by scanner
			e.printStackTrace();
		} catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //connect to specified socket
        try {
            UDPClient.Socket = new DatagramSocket(UDPClient.portNum, UDPClient.ipAddress);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    //thread function to listen for incoming packets
    public void listenSocket() {
        //loop indefinitely
        while (true) {
            System.out.println("RECEIVING");

            //initialize holder for incoming data
            byte[] incomingData = new byte[1024];
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            
            try {
                //receive incoming data
                UDPClient.Socket.receive(incomingPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //deserialize recieved packet
            HACProtocol receivedPacket = null;
            try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(incomingPacket.getData());
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                    receivedPacket = (HACProtocol) objectInputStream.readObject();
                }catch (IOException e) {
                    e.printStackTrace();
                    continue; // skip to the next loop iteration if deserialization fails 
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            
            //access node by IP addy from incomingPacket (through hashmap)
            String senderIP = incomingPacket.getAddress().getHostAddress();
            Node senderNode = nodeMap.get(senderIP + ":" + incomingPacket.getPort());

            //mark time of receipt in sender node
            senderNode.heartbeatRecieved();

            if(senderNode != null) {
                //store the info (update file list)
                senderNode.setFiles(new ArrayList<File>(Arrays.asList(receivedPacket.localFiles)));

                //update node stauts
                boolean[] nodeStatus = receivedPacket.nodesUp;

                //print updated node info (debug)
                System.out.println("Updated Node: " + senderNode.getID() + " - Files: " + senderNode.getFileNames());
                System.out.println("Node Status: " + Arrays.toString(nodeStatus));
            } else {
                 System.out.println("Received packet from unknown node: " + senderIP);
            }
        }
    }

    //thread function to send heartbeat packets to all other nodes
    public void sendPulse() {
        //initialize rng
        SecureRandom random = new SecureRandom();

        //loop indefinitely
        while (true) {             
            //pause for random time (0-30s) before next loop
            try {
                Thread.sleep(random.nextInt(30)*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("SENDING");

            //loop through nodes in node map
            nodeMap.forEach((key, currentNode) -> {
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); //initialize byteArrayOutputStream
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)){ //initialize objectOutput Stream to byteArrayOutputStream
                
                    //access, store, and send local files held on machine using MyFileReader class
                    File localFiles[] = (File[]) MyFileReader.FileReader().toArray(new File[0]);
                    HACProtocol testProtocolPacket = new HACProtocol("P2P", UDPClient.sequenceNum, localFiles);

                    //write protocol packet to byte array output stream
                    try {
                        objectOutputStream.writeObject(testProtocolPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //convert to byte array
                    byte[] serializedObject = byteArrayOutputStream.toByteArray();

                    //send via UDP
                    DatagramPacket packet = new DatagramPacket(serializedObject, serializedObject.length, InetAddress.getByName(currentNode.getIP()), currentNode.getPort());
                    UDPClient.Socket.send(packet);

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });

            //increment sequence num
            UDPClient.sequenceNum++;
        }
    }

    //getters
    public static ConcurrentHashMap<String, Node> getMap() {return nodeMap;}

    public static void main(String[] args) { //nodeNum (0)
        //get node number of client from command line args
        UDPClient.nodeNum = Integer.parseInt(args[0]);

        //initialize client
        UDPClient client = new UDPClient();

        //initialize threadpool
        executorService = Executors.newFixedThreadPool(3);
        
        //heartbeat loop thread

        //execute receiving thread
        executorService.submit(() -> client.listenSocket());

        //execute sending thread
        executorService.submit(() -> client.sendPulse());

        //socket and executor service shut down hook thread
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (Socket != null && !Socket.isClosed()) {
                Socket.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
        }));

        runLoop();

        //perioidically print node data
        while (true) {
            //print header
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("Outputting Network Data");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            //print data for each node
            Collection<Node> nodes = nodeMap.values();
            for (Node currentNode : nodes) {
                System.out.printf("Node: %s is %s%n", currentNode.getID(), currentNode.timeOut()? "DOWN" : "UP");
                System.out.println("\tFiles:");
                System.out.println(currentNode.getFileNames());
            }
            
            //wait 30 seconds
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }   
        }
    }
    static double lastUpdate;
    static Collection<Node> nodeList = nodeMap.values();
    public static void runLoop() {
        lastUpdate = 0;
        while(true) {
            if(System.nanoTime()/1000000000-lastUpdate>=1) {
                lastUpdate = System.nanoTime()/1000000000;
                //Ping all things that need to activate once/sec
                for(Node node: nodeList) { node.heartbeat(); }
            }
        }
    }
}