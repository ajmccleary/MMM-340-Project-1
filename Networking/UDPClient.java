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

import UIElements.Node;

/**
 * 
 * @author cjaiswal
 *
 *  
 * 
 */
public class UDPClient
{
    DatagramSocket Socket;

    //instance variables
    public static int portNum;
    public static Node[] networkNodes = new Node[5];

    //initialize scanner variables
    public static Scanner fileInput;
	public static File inFile = new File("Networking\\ipConfig.txt");
    public static String nextLine;

    public UDPClient() {
        try {
            //initialize scanner
            fileInput = new Scanner(inFile);

            int count = 0;

            do {
                nextLine = fileInput.nextLine();
                Node newNode = new Node(nextLine.substring(0,9), Integer.parseInt(nextLine.substring(10, 14)), new ArrayList<File>());
                
                //check if newNode is node representing this computer
                if (newNode.getPort() != portNum) {
                    //add node to networkNodes array
                    networkNodes[count] = newNode;
                    count++;
                }
            } while (fileInput.hasNextLine());
        } catch (FileNotFoundException e) { //catch potential error thrown by scanner
			e.printStackTrace();
		}

        System.out.println("DEBUG: ");
        for (Node temp : networkNodes) {
            System.out.println(temp.getID());
        }
    } 

    public void createAndListenSocket(int portNum) 
    {
        try 
        {
            Socket = new DatagramSocket(portNum);
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] incomingData = new byte[1024];

            String testArray [] = {""};

            HACProtocol testProtocolPacket = new HACProtocol("doesn'tmatter", 0, 1, testArray);

            SecureRandom random = new SecureRandom();

            // for (int c = 0; c < 3; c++) {
                //write protocol packet to byte array output stream
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(testProtocolPacket);
                objectOutputStream.flush();

                //convert to byte array
                byte[] serializedObject = byteArrayOutputStream.toByteArray();

                //send via UDP
                DatagramPacket packet = new DatagramPacket(serializedObject, serializedObject.length, IPAddress, 9876);
                Socket.send(packet);

                //print object sent and pause before next loop
                // System.out.println("Sent object " + c);
                //Thread.sleep(random.nextInt(30)*1000);
            // }

            System.out.println("Message sent from client");
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            Socket.receive(incomingPacket);
            String response = new String(incomingPacket.getData());
            System.out.println("Response from server:" + response);
            Socket.close();
        }
        catch (UnknownHostException e) 
        {
            e.printStackTrace();
        } 
        catch (SocketException e) 
        {
            e.printStackTrace();
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) { //UDPClient Portnum
        //get portnum of client
        portNum = Integer.parseInt(args[0]);

        //initialize client
        UDPClient client = new UDPClient();

        //create and listen socket with custom port num
        client.createAndListenSocket(portNum);
    }
}

