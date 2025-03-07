package Networking;

import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.*;

/**
 * 
 * @author cjaiswal
 *
 *  
 * 
 */
public class UDPClientCS {
    private DatagramSocket socket;
    private static InetAddress serverAddress; //server's Ip address
    private static int serverPort; // servers port number
    private SecureRandom random; //generating random intervals 

    //initialize scanner variables
	private static File inFile = new File("ipConfig.txt");
    private static String nextLine;

    public UDPClientCS() {
    	try {
            try (Scanner fileInput = new Scanner(inFile)) { //initialize scanner
            //store next line input
            nextLine = fileInput.nextLine();

            //parse input and store in new node
            UDPClientCS.serverAddress = InetAddress.getByName(nextLine.split(" ")[0]);
            UDPClientCS.serverPort = Integer.parseInt(nextLine.split(" ")[1]);
        } catch (FileNotFoundException e) { //catch potential error thrown by scanner
			e.printStackTrace();
		} catch (UnknownHostException e) {
            e.printStackTrace();
        }

        	//UDP socket with a randomly assigned avaiable port
			socket = new DatagramSocket();
            random = new SecureRandom(); //initalizae random number generator 
		} 
    	catch (SocketException e){
			e.printStackTrace();
		}
    }

    public void startClient(){
        try {
            while (true) {
                sendAvailability();  //send client's availability and file listing to the server
                receiveServerResponse(); //wait for and process server updates
                
                // sleep for a random time between 0-30 seconds before sending the next update
                int sleepTime = random.nextInt(31) * 1000; // convert to milliseconds
                System.out.println("Next update in " + (sleepTime / 1000) + " seconds...");
                Thread.sleep(sleepTime); //pause execution for the random interval
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally{
            socket.close(); //close the socket when the client is done
        }
    } 
    
    //sending the avaiablilty of the client to the server
    private void sendAvailability(){
        try {
            HACProtocol availabilityData = generateAvailabilityPacket(); //generate availability data
            
            //serialize the HACProtocol object to a byte array
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(availabilityData);
            objectStream.flush();
            
            byte[] data = byteStream.toByteArray(); //convert to byte array
            
            //create and send a UDP packet to the server
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddress, serverPort);
            socket.send(sendPacket);
            System.out.println("Sent: " + availabilityData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    
    
    //generate a string that represents the client's avaibility and file listing 
    private HACProtocol generateAvailabilityPacket() {
        ArrayList<File> localFiles = MyFileReader.FileReader();
        Node fileNode = new Node("localhost", serverPort, localFiles);
        File[] fileArray = fileNode.getFileList().toArray(new File[0]); // Convert ArrayList<File> to File[]
        return new HACProtocol("1.0", new Random().nextInt(1000), fileArray);
    }

    //recieves and processes the servers response, which clients are avaiabile and which are not 
    private void receiveServerResponse() {
        try {
           byte[] buffer = new byte[1024]; // Buffer to store incoming data
            DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(incomingPacket); // Wait for a packet from the server
            
            // Deserialize the received object
            ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(incomingPacket.getData()));
            HACProtocol response = (HACProtocol) objectStream.readObject();
            Node[] tempNodeArr = response.getNodeArray();
            String parsedResponse = "";
            for(Node node:tempNodeArr) {
                parsedResponse += node.toString() + "\n";
            }
            
            System.out.println("~~~~~~~~~~~~~~~~\nServer Response: \n" + parsedResponse + "\n~~~~~~~~~~~~~~~~~~");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UDPClientCS client = new UDPClientCS();
        client.startClient();
    }
}
