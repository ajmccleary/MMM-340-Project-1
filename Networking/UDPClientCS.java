package Networking;

import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.*;

/**
 * UDPClientCS is a UDP client that communicates with a server
 * to send availability status and receive server responses.
 * It reads server details from an ipConfig.txt file and continuously
 * sends updates at random intervals.
 *
 * This client sends serialized HACProtocol objects containing
 * file availability information and receives updates about
 * active nodes from the server.
 * 
 * @author Andrew McCleary, Aislin Hayes, Brady Galligan
 * 
 */
public class UDPClientCS {
    private DatagramSocket socket;
    private static InetAddress serverAddress;
    private static int serverPort;
    private SecureRandom random;

    private static final File inFile = new File("ipConfig.txt");
    private static String nextLine;

    public UDPClientCS() {
        try (Scanner fileInput = new Scanner(inFile)) {
            //ensure the configuration file exists
            if (!inFile.exists()) {
                throw new FileNotFoundException("Configuration file not found: " + inFile.getAbsolutePath());
            }

            //read the first line of the file and parse IP and port
            nextLine = fileInput.nextLine();
            String[] configParts = nextLine.split(" ");
            if (configParts.length != 2) {
                throw new IllegalArgumentException("Invalid configuration format in ipConfig.txt");
            }

            //resolve the server's address and port
            serverAddress = InetAddress.getByName(configParts[0]);
            serverPort = Integer.parseInt(configParts[1]);

            //create a UDP socket for communication
            socket = new DatagramSocket();
            random = new SecureRandom();

        } catch (FileNotFoundException e) {
            System.err.println("Error: Unable to find configuration file. Please ensure ipConfig.txt exists.");
            System.exit(1); //exit since the client cannot run without server details
        } catch (UnknownHostException e) {
            System.err.println("Error: Invalid server address in ipConfig.txt.");
            System.exit(1); //exit since an invalid address makes communication impossible
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid port number format in ipConfig.txt.");
            System.exit(1); //exit since the port number must be a valid integer
        } catch (SocketException e) {
            System.err.println("Error: Unable to create UDP socket.");
            e.printStackTrace();
            System.exit(1); //exit since a working socket is required for communication
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1); //exit since the configuration file format is incorrect
        }
    }

    //starts the client, sending periodic availability updates and listening for server responses.
    public void startClient() {
        try {
            while (true) {
                sendAvailability(); //send node availability data
                receiveServerResponse(); //listen for server response

                //sleep for a random interval (0-30 seconds) before sending the next update
                int sleepTime = random.nextInt(31) * 1000;
                System.out.println("Next update in " + (sleepTime / 1000) + " seconds...");
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e) {
            System.err.println("Client interrupted. Shutting down...");
            Thread.currentThread().interrupt(); // Restore interrupted status
        } finally {
            socket.close(); //ensure socket is closed before exiting
        }
    }

    //sends availability information to the server.
    private void sendAvailability() {
        try {
            HACProtocol availabilityData = generateAvailabilityPacket();
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(availabilityData);
            objectStream.flush();

            byte[] data = byteStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddress, serverPort);
            socket.send(sendPacket);
            System.out.println("Sent: " + availabilityData);
        } catch (IOException e) {
            System.err.println("Error: Failed to send availability update to server.");
            e.printStackTrace();
        }
    }

    /**
     * Generates an HACProtocol packet containing this node's file listing.
     * returns HACProtocol object with the availability data.
     */
    private HACProtocol generateAvailabilityPacket() {
        //read the list of files available locally
        ArrayList<File> localFiles = MyFileReader.FileReader();
        Node fileNode = new Node("localhost", serverPort, localFiles);
        File[] fileArray = fileNode.getFileList().toArray(new File[0]);
        return new HACProtocol("1.0", new Random().nextInt(1000), fileArray);
    }

    //receives and processes responses from the server
    private void receiveServerResponse() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(incomingPacket); // Wait for response

            //deserialize the received object
            ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(incomingPacket.getData()));
            HACProtocol response = (HACProtocol) objectStream.readObject();
            
            //parse and print the server's response
            StringBuilder parsedResponse = new StringBuilder();
            for (Node node : response.getNodeArray()) {
                parsedResponse.append(node.toString()).append("\n");
            }

            System.out.println("~~~~~~~~~~~~~~~~\nServer Response: \n" + parsedResponse + "\n~~~~~~~~~~~~~~~~~~");

        } catch (IOException e) {
            System.err.println("Error: Failed to receive or process server response.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Received an unknown object from the server.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UDPClientCS client = new UDPClientCS();
        client.startClient();
    }
}