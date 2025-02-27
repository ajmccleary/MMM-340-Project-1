package UIElements;

import java.io.File;
import java.util.ArrayList;

/**
 * This is a node object for storing information on a connected Node/machine
 * @param IPAddress used to store the IP of the connected machine
 * @param containedFiles contains a list of all files on the Node
 * @param portNum int value that stores the port of the connected device
 */
public class Node {
    private String IPAddress;
    private ArrayList<File> containedFiles;
    private boolean hasTimedOut, isSelf;
    private int secSinceHeartbeat;
    private int portNum;
    
    public Node(String IPAddress, int portNum, ArrayList<File> containedFiles) {
        this.IPAddress = IPAddress;
        this.portNum = portNum;

        hasTimedOut = false;
        this.IPAddress = IPAddress;
        this.containedFiles = containedFiles;
        this.isSelf = false;
    }
    
    public void heartbeatRecieved() { secSinceHeartbeat = 0; }
    public void heartbeat() { secSinceHeartbeat++;}
    public boolean timeOut() {
        if(secSinceHeartbeat > 30) {
            hasTimedOut = true;
        }
        return hasTimedOut; 
    }
    public String getFileNames() {
        String directoryList = "";
        for(File file:containedFiles) {
            directoryList += file.getName() + "\n";
        }
        return directoryList;
    }

    public void setToSelf() {this.isSelf = true;}
    public String getID() {return (this.IPAddress + ":" + portNum);}
    public String getIP() { return this.IPAddress; }
    public int getPort() { return this.portNum; }
    public ArrayList<File> getFileList() { return containedFiles; }
    public int getLastHrtBt() { return this.secSinceHeartbeat; }
}
