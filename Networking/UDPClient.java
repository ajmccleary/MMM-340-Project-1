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
    private static HashMap<String,Node> nodeMap = new HashMap<String,Node>();

    //initialize scanner variables
    private static Scanner fileInput;
	private static File inFile = new File("Networking\\ipConfig.txt");
    private static String nextLine;

    public UDPClient() {
        try {
            //initialize count variable
            int count = 0;

            //initialize scanner
            fileInput = new Scanner(inFile);

            //scan through file
            do {
                //store next line input
                nextLine = fileInput.nextLine();

                //parse input and store in new node
                Node newNode = new Node(nextLine.substring(0,9), Integer.parseInt(nextLine.substring(10, 14)), new ArrayList<File>());

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

                count++;
            } while (fileInput.hasNextLine());

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

    public void listenSocket() throws ClassNotFoundException {
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
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)){

                    receivedPacket = (HACProtocol) objectInputStream.readObject();
                }catch (IOException e){
                    e.printStackTrace();
                    continue; // skip to the next loop iteration if deserialization fails 
                }

            //AISLIN access node (through hashmap) by ipadress from incomingpacket, then store its info (in node from incomingpacket using setFileNames) and do the node up node down shit (make a new array of booleans for if a node is up or down)
            
            //access node by IP addy from incomingPacket (through hashmap)
            String senderIP = incomingPacket.getAddress().getHostAddress();
            Node senderNode = nodeMap.get(senderIP);

            if(senderNode != null){
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

            //print data received (will later process data)
            String response = new String(incomingPacket.getData());
            System.out.println("Response from server:" + response);
        }
    }

    public void sendPulse() {
        //initialize rng
        SecureRandom random = new SecureRandom();

        //loop indefinitely
        while (true) {             
            System.out.println("SENDING");

            try {
                //pause for random time (0-30s) before next loop
                Thread.sleep(random.nextInt(30)*1000);

                //loop through nodes in node map
                for (Node currentNode : nodeMap.values()) {
                    //temp make shitty test packet - use myFileReader method to get and then store files
                    File testArray[] = (File[])MyFileReader.FileReader().toArray();
                    HACProtocol testProtocolPacket = new HACProtocol("doesn'tmatter", 0, 1, testArray);

                    //write protocol packet to byte array output stream
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                    objectOutputStream.writeObject(testProtocolPacket);

                    //convert to byte array
                    byte[] serializedObject = byteArrayOutputStream.toByteArray();

                    //send via UDP
                    DatagramPacket packet = new DatagramPacket(serializedObject, serializedObject.length, InetAddress.getByName(currentNode.getIP()), currentNode.getPort());
                    Socket.send(packet);

                    //close out resources
                    byteArrayOutputStream.close();
                    objectOutputStream.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //getters
    public static HashMap<String, Node> getMap() {return nodeMap;}

    public static void main(String[] args) { //nodeNum (0)
        //get node number of client from command line args
        UDPClient.nodeNum = Integer.parseInt(args[0]);

        //initialize client
        UDPClient client = new UDPClient();

        //initialize threadpool
        executorService = Executors.newFixedThreadPool(2);

        //execute receiving thread
        executorService.submit(() -> {
            try {
                client.listenSocket();
            } catch (ClassNotFoundException ex) {
            }
        });

        //execute sending thread
        executorService.submit(() -> client.sendPulse());

        //logic for determining if a node is down here - in seperate thread?

        //perioidically print node data - in seperate thread?
    }
}