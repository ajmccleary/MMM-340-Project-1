package Networking;

import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

/**
 * 
 * A UDP client peer to peer network implementation.
 * Contains a listening thread, sending thread, down detection thread, and printing thread (end of main).
 * @author Mischievous Mushroom Men
 * 
 */
public class UDPClientP2P {
    //class variables
    private static DatagramSocket Socket;
    private static ExecutorService executorService;
    private static int nodeNum; //0-6, correlate to line of ipconfig to assign socket to
    private static int portNum;
    private static int sequenceNum = 0;
    private static double lastUpdate;
    private static ConcurrentHashMap<String,Node> nodeMap = new ConcurrentHashMap<String,Node>();
    

    //initialize scanner variables
	private static File inFile = new File("ipConfig.txt");
    private static String nextLine;

    /**
     * Constructor for UDPClient object.
     * Reads in from ipConfig.txt to get IPs and port nums of other nodes before storing them in the nodeMap.
     * Initializes and binds socket to the IP and port number specified by command line input.
     */
    public UDPClientP2P() {
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
                if (count == UDPClientP2P.nodeNum) {
                    newNode.setToSelf();
                    UDPClientP2P.portNum = newNode.getPort();
                }

                //check if newNode is NOT node representing this computer
                if (!newNode.isNodeSelf()) {
                    //add node to hash map (key is id)
                    nodeMap.put(newNode.getID(), newNode);
                }

                //increment count
                count++;
            } while (fileInput.hasNextLine());
        } catch (FileNotFoundException e) { //catch potential error thrown by scanner
			e.printStackTrace();
        }

        //connect to specified socket
        try {
            UDPClientP2P.Socket = new DatagramSocket(UDPClientP2P.portNum);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    
    /**
     * Thread function which runs a loop on a thread that triggers once/sec to uptick node heartbeat.
     */
    public static void runLoop() {
        Collection<Node> nodeList = nodeMap.values();
        lastUpdate = 0;
        while(true) {
            if(System.nanoTime()/1000000000-lastUpdate>=1) {
                lastUpdate = System.nanoTime()/1000000000;
                //Ping all things that need to activate once/sec
                for(Node node: nodeList) { node.heartbeat(); }
            }
        }
    }

    /**
     * Thread function which continously listens for incoming packets,
     * then deserializes them and stores their data upon reception.
     */
    public void listenSocket() {
        //loop indefinitely
        while (true) {
            //initialize holder for incoming data
            byte[] incomingData = new byte[1024];
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            
            try {
                //receive incoming data
                UDPClientP2P.Socket.receive(incomingPacket);
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

            
            if(senderNode != null) {
                //mark time of receipt in sender node
                senderNode.heartbeatRecieved();

                //store the info (update file list)
                senderNode.setFiles(new ArrayList<File>(Arrays.asList(receivedPacket.localFiles)));
            } else {
                 System.out.println("Received packet from unknown node: " + senderIP);
            }
        }
    }

    /**
     * Thread function which sends packets to all other nodes on the network.
     * Runs indefinitely, sending packets at random 0-30 second intervals.
    */
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

            //loop through nodes in node map
            nodeMap.forEach((key, currentNode) -> {
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); //initialize byteArrayOutputStream
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)){ //initialize objectOutput Stream to byteArrayOutputStream
                
                    //access, store, and send local files held on machine using MyFileReader class
                    File localFiles[] = (File[]) MyFileReader.FileReader().toArray(new File[0]);
                    HACProtocol testProtocolPacket = new HACProtocol("P2P", UDPClientP2P.sequenceNum, localFiles);

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
                    UDPClientP2P.Socket.send(packet);

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });

            //increment sequence num
            UDPClientP2P.sequenceNum++;
        }
    }

    /**
     * Main method to start UDP Peer To Peer Client.
     * Initializes client object, starts the heartbeat thread,
     * the listening thread, and the sending thread. Ends with the printing thread.
     * 
     * @param args First and only command line argument is the node number, 
     * which correlates to the line of ipconfig to bind socket to.
     */
    public static void main(String[] args) { //nodeNum (0)
        //get node number of client from command line args
        UDPClientP2P.nodeNum = Integer.parseInt(args[0]);

        //initialize client
        UDPClientP2P client = new UDPClientP2P();

        //initialize threadpool
        executorService = Executors.newFixedThreadPool(3);
        
        //heartbeat loop thread
        executorService.submit(() -> UDPClientP2P.runLoop());

        //execute receiving thread
        executorService.submit(() -> client.listenSocket());

        //execute sending thread
        executorService.submit(() -> client.sendPulse());

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
}