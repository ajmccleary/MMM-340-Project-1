package Networking;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This represents a node object on a network.
 * It stores information on a connected node/machine,
 * including ip address, port number, file listing, and status.
 * 
 * @author Andrew McCleary, Aislin Hayes, Brady Galligan
 * 
 */
public class Node implements Serializable{
    //instance variables
    private String IPAddress;
    private ArrayList<File> containedFiles;
    private boolean hasTimedOut, isSelf;
    private int secSinceHeartbeat;
    private int portNum;
    
    /**
     * Node object constructor.
     * 
     * @param IPAddress used to store the IP of the connected machine
     * @param containedFiles contains a list of all files on the Node
     * @param portNum int value that stores the port of the connected device
     */
    public Node(String IPAddress, int portNum, ArrayList<File> containedFiles) {
        this.IPAddress = IPAddress;
        this.portNum = portNum;
        this.hasTimedOut = false;
        this.IPAddress = IPAddress;
        this.containedFiles = containedFiles;
        this.isSelf = false;
    }
    
    /**
     * Checks if the node has timed out.
     * A node is timed out if no heartbeat has been received for 30+ seconds.
     * 
     * @return True IF node is down, otherwise false if up
     */
    public boolean timeOut() {
        if(secSinceHeartbeat > 30) {
            hasTimedOut = true;
        }
        return hasTimedOut; 
    }

    /**
     * Getter for formatted string listing all files stored on the node.
     * 
     * @return A string containing the names of all files on the node.
     */
    public String getFileNames() {
        String directoryList = "";
        for(File file:containedFiles) {
            directoryList += "\t" + file.getName() + "\n";
        }
        return directoryList;
    }

    /**
     * Getter for isSelf, used to check if node represents the machine running the program.
     * 
     * @return true if node respresents running machine, otherwise false.
     */
    public Boolean isNodeSelf() {
        return this.isSelf;
    }
    
    /**
     * Getter for a string representation of the node, including its ID and file list.
     * 
     * @return A string repreentation of the node.
     */
    @Override
    public String toString() {
        return "ID: " + this.IPAddress + ":" + this.portNum + "Is Online?: " + this.timeOut() + "\nFile List: \n" + this.getFileNames();
    }

    /**
     * Resets the Nodes heartbeat clock and marks it active.
     */
    public void heartbeatRecieved() {
        secSinceHeartbeat = 0; hasTimedOut = false;
    }

    /**
     * Increments heartbeat clock. Called once per second.
     */
    public void heartbeat() {
        secSinceHeartbeat++;
    }

    /**
     * Setter for file library on a node.
     * 
     * @param files The new list of files
     */
    public void setFiles(ArrayList<File> files) {
        this.containedFiles = files;
    }

    /**
     * Setter for isSelf, marks the node as representing the running machine.
     */
    public void setToSelf() {
        this.isSelf = true;
    }

    /**
     * Getter for the stored unique id of the node.
     * 
     * @return The unique id of the node, formatted as <ipaddress>:<portnumber>.
     */
    public String getID() {
        return (this.IPAddress + ":" + portNum);
    }

    /**
     * Getter for the stored ip address of the node.
     * 
     * @return The ip stored on the node.
     */
    public String getIP() {
        return this.IPAddress; 
    }

    /**
     * Getter for the stored port number of the node.
     * 
     * @return The port number stored on the node.
     */
    public int getPort() {
        return this.portNum;
    }

    /**
     * Getter for the list of files.
     * 
     * @return The list of files stored on node.
     */
    public ArrayList<File> getFileList() {
        return containedFiles;
    }

    /**
     * Getter for the number of seconds since the last heartbeat was received.
     * 
     * @return The number of seconds since the lsat heartbeat.
     */
    public int getLastHrtBt() {
        return this.secSinceHeartbeat;
    }
}
