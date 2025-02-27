package Networking;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public static DatagramSocket Socket;
    public static ExecutorService executorService;
    public static int portNum;
    public int numNodes = 5;
    public Node[] networkNodes = new Node[numNodes];

    //initialize scanner variables
    public static Scanner fileInput;
	public static File inFile = new File("Networking\\ipConfig.txt");
    public static String nextLine;

    //initialize threads
    class SendingThread implements Runnable {
        UDPClient client;

        public SendingThread(UDPClient client) {
            this.client = client;
        }
    
        @Override
        public void run() {
            this.client.createAndListenSocket();
        }
    }

    //receivingThread

    public UDPClient() {
        try {
            //initialize scanner
            fileInput = new Scanner(inFile);

            //initialize count variable for number of nodes processed
            int nodeCount = 0;

            //scan through file
            do {
                //store next line input
                nextLine = fileInput.nextLine();

                //parse input and store in new node (along with files STILL NEED TO DO THAT)
                Node newNode = new Node(nextLine.substring(0,9), Integer.parseInt(nextLine.substring(10, 14)), new ArrayList<File>());
                
                //check if newNode is NOT node representing this computer
                if (newNode.getPort() != UDPClient.portNum) {
                    //add node to networkNodes array
                    this.networkNodes[nodeCount] = newNode;
                    nodeCount++;
                }
            } while (fileInput.hasNextLine());

        } catch (FileNotFoundException e) { //catch potential error thrown by scanner
			e.printStackTrace();
		}
    } 

    public void createAndListenSocket() {
        try {
            Socket = new DatagramSocket(UDPClient.portNum);
            SecureRandom random = new SecureRandom();

            for (Node currentNode : this.networkNodes) {
                InetAddress IPAddress = InetAddress.getByName(currentNode.getIP());

                String testArray [] = {""};

                HACProtocol testProtocolPacket = new HACProtocol("doesn'tmatter", 0, 1, testArray);

                //write protocol packet to byte array output stream
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(testProtocolPacket);

                //convert to byte array
                byte[] serializedObject = byteArrayOutputStream.toByteArray();

                //send via UDP
                DatagramPacket packet = new DatagramPacket(serializedObject, serializedObject.length, IPAddress, currentNode.getPort());
                Socket.send(packet);

                //pause for random time (0-30s) before next loop
                Thread.sleep(random.nextInt(30)*1000);

                //close out resources
                byteArrayOutputStream.close();
                objectOutputStream.close();
            }            

            //to be moved to ListeningThread
            byte[] incomingData = new byte[1024];
            System.out.println("Message sent from client");
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            Socket.receive(incomingPacket);
            String response = new String(incomingPacket.getData());
            System.out.println("Response from server:" + response);

            //close Socket
            Socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } 
        catch (SocketException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) { //Portnum (0)
        //get portnum of client
        UDPClient.portNum = Integer.parseInt(args[0]);

        //initialize client
        UDPClient client = new UDPClient();

        //initialize threadpool
        executorService = Executors.newFixedThreadPool(2); // Create a thread pool with 2 threads (may modify)

        //create and listen socket with custom port num
        client.createAndListenSocket();

        //execute sending thread
        executorService.execute(client.new SendingThread(client));
    }
}