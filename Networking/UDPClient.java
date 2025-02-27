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

import UIElements.MainScreen;
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

    //initialize scanner variables
    public static Scanner fileInput;
	public static File inFile = new File("Networking\\ipConfig.txt");
    public static String nextLine;

    public UDPClient() {
        try {
            UDPClient.Socket = new DatagramSocket(UDPClient.portNum);

            //initialize scanner
            fileInput = new Scanner(inFile);

            //scan through file
            do {
                //store next line input
                nextLine = fileInput.nextLine();

                //parse input and store in new node
                Node newNode = new Node(nextLine.substring(0,9), Integer.parseInt(nextLine.substring(10, 14)), new ArrayList<File>());
                
                //check if newNode is NOT node representing this computer
                if (newNode.getPort() != UDPClient.portNum) { //need to change to take IP into account as well
                    //add node to hash map (key is id)
                    MainScreen.getMap().put(newNode.getID(), newNode);
                }
            } while (fileInput.hasNextLine());

        } catch (FileNotFoundException e) { //catch potential error thrown by scanner
			e.printStackTrace();
		} catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void listenSocket() {
        //loop indefinitely
        while (true) {
            System.out.println("RECEIVING");

            //initialize holder for incoming data
            byte[] incomingData = new byte[1024];
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);

            //receive incoming data
            try {
                UDPClient.Socket.receive(incomingPacket); //UDPClient.Socket
            } catch (IOException e) {
                e.printStackTrace();
            }

            //AISLIN access node (through hashmap) by ipadress from incomingpacket, then store its info (in node from incomingpacket using setFileNames) and do the node up node down shit (make a new array of booleans for if a node is up or down)
            

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
            try {
                System.out.println("SENDING");
                //pause for random time (0-30s) before next loop
                Thread.sleep(random.nextInt(30)*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                for (Node currentNode : MainScreen.getMap().values()) {
                    //get IP adress from current node
                    InetAddress IPAddress = InetAddress.getByName(currentNode.getIP());

                    //temp make shitty test packet - use myFileReader method to get and then store files
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

                    //close out resources
                    byteArrayOutputStream.close();
                    objectOutputStream.close();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } 
            catch (SocketException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) { //Portnum (0)
        //get portnum of client
        UDPClient.portNum = Integer.parseInt(args[0]);

        //initialize client
        UDPClient client = new UDPClient();

        //initialize threadpool
        executorService = Executors.newFixedThreadPool(2);

        //execute receiving thread
        executorService.submit(() -> client.listenSocket());

        //execute sending thread
        executorService.submit(() -> client.sendPulse());

        //logic for determining if a node is down here - in seperate thread?

        //perioidically print node data - in seperate thread?
    }
}