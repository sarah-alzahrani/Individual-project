

import java.net.InetSocketAddress;
import java.net.Socket;

/*
*
* @author Sarah Alzahrani
*/

public interface PeerChat {

    public void init(Socket socket, int uid); // initialise with a TCP server socket and unique id for node
    
    public long joinNetwork(InetSocketAddress bootstrap_node); //returns network_id, a locally generated number to identify peer network
   
    public boolean leaveNetwork(int nodeID); // parameter is previously returned peer network identifier
   
    public void chat(String text, String[] tags);
    
    public ChatResult[] getChat(String[] tags);
    
}
