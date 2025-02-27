package UIElements;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

import Networking.MyFileReader;


public class MainScreen extends JFrame{
    private double lastUpdate;
    private ArrayList<Node> nodeList; 
    private ArrayList<NodeInfoScreen> infoList; //Will be obsolete when map is fully implemented
    private HashMap<String,Node> nodeMap;

    private NodeInfoScreen nodeInfoScreen;
    public MainScreen() {
        super("Node Stats");
        nodeList = new ArrayList<Node>();
        nodeMap = new HashMap<String,Node>();
        infoList = new ArrayList<NodeInfoScreen>();
        setLayout(new GridLayout(1, 6)); 


        
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }
    public void addNode(Node node) {
        nodeList.add(node);
        nodeMap.put(node.getID(), node);
        infoList.add(new NodeInfoScreen(this, node));
        this.add(infoList.getLast());
    }

    public ArrayList<Node> getList() { return this.nodeList; }
    public HashMap<String, Node> getMap() {return this.nodeMap;}

    public static void main(String[] args) {
        MainScreen mainScreen = new MainScreen();
        //Test nodes
        mainScreen.addNode(new Node("198.3.6.7", 1234, MyFileReader.FileReader()));
        mainScreen.addNode(new Node("198.3.6.7", 2345, MyFileReader.FileReader()));
        mainScreen.addNode(new Node("198.3.6.7", 3456, MyFileReader.FileReader()));
        //Will get removed on final implementation
        mainScreen.runLoop();
    }
    private void runLoop() { //Triggers once per second
        lastUpdate = 0;
        while(true) {
            if(System.nanoTime()/1000000000-lastUpdate>=1) {
                System.out.println("DEBUG: Second has passed");
                lastUpdate = System.nanoTime()/1000000000;
                //Ping all things that need updates once/sec here
                for(NodeInfoScreen infoItem: infoList) { infoItem.refresh(); infoItem.getNode().heartbeat();}
            }
        }
    }
}