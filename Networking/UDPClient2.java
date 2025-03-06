package Networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * 
 * @author cjaiswal
 *
 *  
 * 
 */
public class UDPClient2 {
    private DatagramSocket socket;
    private InetAddress serverAddress; //server's Ip address
    private int serverPort = 9876; // servers port number
    private Random random; //generating random intervals 

    public UDPClient2() 
    {
    	try {
        	//UDP socket with a randomlu assigned avaiable port
			socket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localHost"); //sever running locally 
            random = new Random(); //initalizae random number generator 
		} 
    	catch (SocketException e){
			e.printStackTrace();
		}
        catch(UnknownHostException e){
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



     private File[] listFilesInDirectory(String directory) {
        List<File> outFiles = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            outFiles = paths.filter(Files::isRegularFile)
                            .map(Path::toFile)
                            .collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("DEBUG: Error reading files in directory " + directory);
        }
        return outFiles.toArray(new File[0]);
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
            
            System.out.println("Server Response: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) 
    {
        UDPClient2 client = new UDPClient2();
        client.startClient();
    }
}
