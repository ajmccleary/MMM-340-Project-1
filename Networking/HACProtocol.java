package Networking;

import java.net.*;

public class HACProtocol { //what class format? javadoc needed? tostring etc needed?
    //protocol specific fields
    String version; //protocol version (do we need this?)
    int length; //length of data
    int numNodes; //number of nodes on network
    boolean nodesUp []; //array of booleans representing status of all other nodes
    String localFiles []; //array of file names on node

    //existing packet format
    DatagramPacket packet;

    public HACProtocol (byte[] data, InetAddress address, int port, String version, int length, int numNodes, String[] localFiles) {
        //create and store packet
        this.packet = new DatagramPacket(data, data.length, address, port);

        //initialize protocol variables
        this.version = version;
        this.length = length;
        this.numNodes = numNodes; //how do i get this? is constructor ideal?
        this.nodesUp = new boolean[numNodes];
        this.localFiles = localFiles;
    }
}
