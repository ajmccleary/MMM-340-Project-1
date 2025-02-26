package UIElements;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.JFrame;

import Networking.MyFileReader;


public class MainScreen extends JFrame{
    private double lastUpdate;
    private ArrayList<Node> nodeList; 

    private NodeInfoScreen nodeInfoScreen;
    public MainScreen() {
        super("Node Stats");
        nodeList = new ArrayList<Node>();
        setLayout(new GridLayout(1, 6)); 
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }
    public void addNode(Node node) {
        nodeList.add(node);
        this.add(new NodeInfoScreen(this, node));
    }

    public ArrayList<Node> getList() { return this.nodeList; }

    public static void main(String[] args) {
        MainScreen mainScreen = new MainScreen();
        mainScreen.addNode(new Node("198.3.6.7", 1234, MyFileReader.FileReader()));
        mainScreen.runLoop();
    }
    private void runLoop() {
        lastUpdate = 0;
        while(true) {
            if(System.nanoTime()/1000000000-lastUpdate>=1) {
                lastUpdate = System.nanoTime()/1000000000;
                //Ping all things that need updates once/sec here
                for(Node node:nodeList) { node.heartbeat(); }
            }
        }
    }
}