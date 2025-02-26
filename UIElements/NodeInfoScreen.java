package UIElements;
/**
 * Provides information about a node (last recieved heartbeat, and list of files in it's directory)
 * Format:
 *     Node Name/Identifier
 *     "Last Heartbeat: " + Time Since Last Heartbeat (Live Updating)
 *      List of Files on Respective Node
 */     

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.*;

public class NodeInfoScreen extends JPanel{
    private MainScreen frame;
    private JLabel IP, hrtBt, fileList;
    private Node node;
    Dimension size;

    public NodeInfoScreen(MainScreen frame, Node node) {
        this.setLayout(new GridLayout(3, 1));
        this.frame = frame; 
        this.node = node;
        size = new Dimension(80,200);

        IP = new JLabel("");
        hrtBt = new JLabel("");
        fileList = new JLabel("");
        IP.setText("IP: " + node.getIP());
        hrtBt.setText("Last Heartbeat Recieved " + node.getLastHrtBt() + " seconds ago");
        fileList.setText("Node File List: " + node.getFileNames());

        this.add(IP);
        this.add(hrtBt);
        this.add(fileList);

        this.setSize(this.size);
        this.setPreferredSize(this.size);
    }
    public void refresh() {
        IP.setText("IP: " + node.getIP());
        hrtBt.setText("Last Heartbeat Recieved " + node.getLastHrtBt() + " seconds ago");
        fileList.setText("Node File List: " + node.getFileNames());
        this.revalidate();
        this.repaint();
    }
}
