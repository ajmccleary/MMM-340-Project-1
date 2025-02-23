package Networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ProtocolTestingServer {

    public static void main (String args[]) {
        int received = 0;
        while(received < 3) {
            try (ServerSocket serverSocket = new ServerSocket(12345);
            Socket socket = serverSocket.accept();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            Object receivedObject = ois.readObject();
            System.out.println(receivedObject.toString()); //will likely print in another cmd tab
            received++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
