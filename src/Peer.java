
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 *
 * @author Sarah Alzahrani
 */


/*
 * This class reflect the given project specification and implement the peerChat interface 
 */
public class Peer implements PeerChat {
    
	int nodeID;
	int counter=1; //locally generated number to identify peer network
	Thread threadHandler;
	static long network_id; 
	//routing table represented as a TreeMap to facilitate the prefix routing
	TreeMap<Integer, InetSocketAddress> routingTable = new TreeMap<Integer, InetSocketAddress>(); 
	Socket socket;
	private Socket JoinSocket;
	private Socket targetSocket;
	InetSocketAddress bootstrap_node; 
	Integer bootstrap_ID;
	private InetAddress targetNode; 
	
	
   /*
    * Constructors 
    */
    public Peer() {
    	super();
	}
	public Peer (int nodeID){
		this.nodeID=nodeID;
	}


	/*
     * (non-Javadoc)
     * The following function implements hashing of an arbitrary string
     * hashCode method is used for string hashing
     */
    public int hashCode(String str) {
    int hash = 0;
    for (int i = 0; i < str.length(); i++) {
    hash = hash * 31 + str.charAt(i);}
    return Math.abs(hash);}
    

    /*
    * (non-Javadoc)
    * @see PeerChat#init(java.net.Socket, int)
    * initialise with a TCP server socket and unique id for node
    * init method is used for the passing of a TCP server socket
    */
    public void init(Socket socket, int uid){
    	this.socket = socket;
    	threadHandler = new threadHandler(socket);
    	threadHandler.start();
		routingTable.put(nodeID, new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort()));
    	} 
    
    
    /*
     * (non-Javadoc)
     * @see PeerChat#joinNetwork(java.net.InetSocketAddress)
     * A node wishing to join the network must have the ip address of the bootstrap node 
     * this method returns network_id, a locally generated number to identify peer network
     */
    public long joinNetwork(InetSocketAddress bootstrap_node){
    	
    	JSONObject joinMessage = new JSONObject();
		try {
		joinMessage.put("type", "JOINING_NETWORK");
		joinMessage.put("node_id", String.valueOf(nodeID));
	    joinMessage.put("ip_address", InetAddress.getLocalHost().toString()); // the ip address of the joining node
		String ip=bootstrap_node.getAddress().toString(); // the ip address of the bootstrap node
		JoinSocket = new Socket(ip,8767); //All communication will be via TCP to port 8767
		DataOutputStream toBootstrapNode= new DataOutputStream(JoinSocket.getOutputStream());
		toBootstrapNode.writeUTF(joinMessage.toString());// write the message to the socket
		network_id= counter; // locally generated number to identify the peer 
		counter++;
		} 
		catch (JSONException e){
			e.printStackTrace();} 
		catch (UnknownHostException e){
			e.printStackTrace();} 
		catch (IOException e) {
			e.printStackTrace();}
		
    	return network_id;
    	} 
   
    /*
     * (non-Javadoc)
     * @see PeerChat#chat(java.lang.String, java.lang.String[])
     * this method used to send chat messages to the other peers in the network
     * tags and hashing them are not considered in this phase of implementation
     * only pure message of text is send to other peers.
     */
    public void chat(String text, String[] tags){
    	try{
		JSONObject ChatMessage = new JSONObject();
		ChatMessage.put("type", "CHAT");
		ChatMessage.put("sendingNode_id", nodeID);
		ChatMessage.put("text", text);
		//Send Chat message to all nodes.
    	for (java.util.Map.Entry<Integer, InetSocketAddress> entry : routingTable .entrySet()) {
		send(ChatMessage, entry.getKey());}}
    	catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  
   
    /*
     * (non-Javadoc)
     * @see PeerChat#leaveNetwork(long)
     * when Node planning to leave the network this method will send a LEAVING_NETWORK message to the nodes in it's routing table.
     */
        public boolean leaveNetwork(int nodeID){
    	try {		
		JSONObject leaveMessage = new JSONObject();
		leaveMessage.put("type", "LEAVING_NETWORK");
		leaveMessage.put("node_id", nodeID);
    	for (java.util.Map.Entry<Integer, InetSocketAddress> entry : routingTable .entrySet()) {
		send(leaveMessage, entry.getKey());} // inform the other node about its leaving
    	}
    	catch (JSONException e) {
			e.printStackTrace();}
    	catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    
		return true;
	}
        
       /*
        * (non-Javadoc)
        * @see PeerChat#getChat(java.lang.String[])
        * the following method is not considered during this phase of the project
        */
        public ChatResult[] getChat(String[] tags){return null;}



    /*
     * This method is used to send a JSON Message to the closet neighbor of specific node (higher or lower in the tree)
     */
    private void send(JSONObject Message, Integer key) throws Exception, IOException {
    	InetSocketAddress targetNode; 
    	String targetNodeIP;
    	DataOutputStream sendingSocket;
    	targetNode=routingTable.get(findClosestNode(nodeID));//find closest node -- prefix routing
		targetNodeIP=targetNode.getAddress().toString(); 
		targetSocket = new Socket(targetNodeIP,8767); //sending a message to closet node 
		sendingSocket= new DataOutputStream(targetSocket.getOutputStream());
		sendingSocket.writeUTF(Message.toString());
		sendingSocket.close(); }
    

    /*
     * This method is used to navigate through the tree map and find the closest node id to specific node
     * this method is an attempt to build the prefix routing but it is not complete
     * passing the message on to a node at least one 'digit' closer.
     * there should be a loop somewhere in the code to route the message until it hit the final destination
     */
	private Integer findClosestNode(int nodeID2) {
		Integer result = null,absLower,absHigher = 0;
		java.util.Map.Entry<Integer, InetSocketAddress> lowerKey;
		java.util.Map.Entry<Integer, InetSocketAddress> higherKey;
		
		lowerKey= routingTable.lowerEntry(nodeID2); // get a lower key
		higherKey= routingTable.higherEntry(nodeID2); // get a higher key
		
		if (lowerKey != null && higherKey != null) { // identify which node key is closer to the node the lower or the higher
			absLower=Math.abs(nodeID2 - lowerKey.getKey());
			absHigher=Math.abs(nodeID2 - higherKey.getKey());
			if(absHigher > absLower)
			lowerKey.getKey();
			else higherKey.getKey();}
		
		else if (lowerKey != null || higherKey != null) {
			if (lowerKey != null)
				result = lowerKey.getKey();
			else result = higherKey.getKey();}
		
		else 
		result=bootstrap_ID; // in case no neighbor found the message will be send to the bootstrap node
		return result;
	}

	
	
	@SuppressWarnings("unused")
	/*
	 * PING passed through the network following a failed transmission of another message, testing and pruning the route. 
	 * PING message proceeds in the normal way through the overlay network. At each stage, on receipt of a PING message, 
	 * a node should send an ACK message directly to the immediate sender of the PING message 
	 */
	private void ping(JSONObject PINGMessage , String targetNodeIP){
		
		JSONObject ping = new JSONObject(); // create the JSON Message 
		try {
		ping.put("type","PING");
		ping.put("sender_id", this.nodeID); 
		ping.put("target_id", PINGMessage.get("node_id")); 
		ping.put("ip_address", PINGMessage.get("ip_address")); 
		targetNode = null;
		
		targetNodeIP = targetNode.getAddress().toString(); 
		targetSocket = new Socket(targetNodeIP,8767); 
		DataOutputStream sendingSocket = new DataOutputStream(targetSocket.getOutputStream());
		sendingSocket.writeUTF(ping.toString());
		sendingSocket.wait(3000); //should be ACK within 30s
		
		DataInputStream recieveSocket = new DataInputStream(socket.getInputStream());
		String recievedMessage = recieveSocket.readUTF();
		if (recievedMessage.length()<1) {  
		/*
		 *  if nothing is coming the receiving socket after the timeout period the this node will 
		 *  be considered is unreached and it will be remove from the routing table
		 */
		routingTable.remove(PINGMessage.get("node_id").toString()); 
		}else
		System.out.println("ACK recieved");} 
		
		catch (JSONException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}

	/*
	 * this class handle the incoming Messages and provide the apropriate responses to each of them 
	 * based on the message type 
	 */
	
	private class threadHandler extends Thread { 
    private Socket socket;
    
    public threadHandler(Socket socket) {
    this.socket=socket;	}

    public void run() {
	
		try {
		DataInputStream recieveSocket = new DataInputStream(socket.getInputStream());
		String recievedMessage = recieveSocket.readUTF();
		JSONObject Message = new JSONObject(recievedMessage);
		String type= Message.get("type").toString();
		
		switch (type) {
	
		case "JOINING_NETWORK":
			JSONObject JoinMessage;
			int node_id=(Integer)Message.get("node_id");
			InetSocketAddress ip= (InetSocketAddress) Message.get("ip_address");
			JoinMessage = new JSONObject();
			JoinMessage.put("type", "JOINING_NETWORK");
			JoinMessage.put("node_id", node_id);
			JoinMessage.put("ip_address", ip);
			routingTable.put(node_id,ip);//add node to the routing table
			send(JoinMessage, node_id); //send the join message and the node id to the neighbor in the routing table.
			break;
		
		case "LEAVING_NETWORK":
	    	routingTable.remove((Integer)Message.get("node_id")); //remove node by key which its nodeID
			break;
			
		case "CHAT":
			System.out.println("Message recieved From: " + Message.get("sendingNode_id"));
			System.out.println(Message.get("text") +"/n");
			break;
        
		case "JOINING_NETWORK_RELAY":	
			JSONObject JoinMessageRely = new JSONObject();
			JoinMessageRely.put("type", "JOINING_NETWORK_RELAY");
			JoinMessageRely.put("node_id",Message.getString("node_id"));
			JoinMessageRely.put("gateway_id",Message.getString("gateway_id"));
			send(JoinMessageRely, Integer.getInteger(Message.getString("node_id")));
			break;
			
		case "ROUTING_INFO":
			//merging any routing information it receives from other nodes as it arrives.
			JSONArray RouteTable = (JSONArray) Message.get("route_table");
			for (int i = 0; i < RouteTable.length(); i++) {
			JSONObject entry = RouteTable.getJSONObject(i);
			int node_id1=Integer.valueOf(entry.getString("node_id"));
			routingTable.put(node_id1,new InetSocketAddress(entry.getString("ip_address"), 8767));}
			break;
			
		case "PING":
			JSONObject ACKMessage;
			ACKMessage = new JSONObject();
			ACKMessage.put("type", "ACK"); 
			ACKMessage.put("node_id", Message.get("target_id")); 
			ACKMessage.put("ip_address", InetAddress.getLocalHost().getHostAddress().toString()); 
			targetSocket = new Socket("localhost",8767); 
			DataOutputStream sendingSocket = new DataOutputStream(targetSocket.getOutputStream());
			sendingSocket.writeUTF(ACKMessage.toString());
			sendingSocket.close();
			break;
		
		case "ACK":
			System.out.println("ACK Recieved: " + Message.toString());
			break;
			
		case "ACK_CHAT":
			break;
		case "CHAT_RETRIEVE":
			break;
		case "CHAT_RESPONSE":
			break;
	    default:
	    	break;
		} //end switch
		} //end try
		catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
}// end run
    }//end threadHandler
}
