package Networking;

import java.io.File;
import java.io.Serializable;

public class HACProtocol implements Serializable { //what class format? javadoc needed? tostring etc needed?
    //protocol control fields
    String version; //protocol version (do we need this?)
    int sequenceNumber; //sequence of packet sent

    //protocol data
    boolean nodesUp []; //array of booleans representing status of all other nodes
    File localFiles []; //array of file names on node
    Node nodeArr[]; //Array of nodes for Client Server


    //packet constructor - P2P
    public HACProtocol (String version, int sequenceNumber, File[] localFiles) {
        //initialize protocol control fields
        this.version = version;

        //initialize protocol data
        this.nodesUp = new boolean[6];
        this.localFiles = localFiles; //input using Brady method
    }

    //packet constructor - CS
    public HACProtocol(String version, int sequenceNumber, Node[] nodeArr) {
        this.version = version;
        this.nodesUp = new boolean[6];
        this.nodeArr = nodeArr;
    }

    public File[] getLocalFiles() { return localFiles; }
    public Node[] getNodeArray() { return nodeArr; }
}
