package Networking;

import java.io.File;
import java.io.Serializable;
/**
 * Packet protocol that stores all data
 * to be sent over the network
 * @author Mischevious Mushroom Men
 */
public class HACProtocol implements Serializable { //what class format? javadoc needed? tostring etc needed?
    //protocol control fields
    String version; //protocol version (do we need this?)
    int sequenceNumber;

    //protocol data
    boolean nodesUp []; //array of booleans representing status of all other nodes
    File localFiles []; //array of file names on node
    Node nodeArr[]; //Array of nodes for Client Server


    /**
     * Constructor for HACProtocol packet
     * @param version
     * @param sequenceNumber
     * @param localFiles
     */
    public HACProtocol (String version, int sequenceNumber, File[] localFiles) {
        //initialize protocol control fields
        this.version = version;
        this.sequenceNumber = sequenceNumber;

        //initialize protocol data
        this.nodesUp = new boolean[6];
        this.localFiles = localFiles; //input using Brady method
    }

    /**
     * Constructor for HACProtocol packet
     * @param version
     * @param sequenceNumber
     * @param nodeArr
     */
    public HACProtocol(String version, int sequenceNumber, Node[] nodeArr) {
        this.version = version;
        this.sequenceNumber = sequenceNumber;
        this.nodesUp = new boolean[6];
        this.nodeArr = nodeArr;
    }

    /**
     * Getter for file list contained in the packet
     * @return The array of files contained in the packet
     */
    public File[] getLocalFiles() { return localFiles; }
    /**
     * Getter for the Node list contained in the packet
     * @return The array of Nodes contained in the packet
     */
    public Node[] getNodeArray() { return nodeArr; }
}
