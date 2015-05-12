import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/*
 *
 * @author Sarah Alzahrani
 */
public class Test {
	
	private static ServerSocket serverSocket;

	public static void main(String[] args) {
		int nodeID;
		InetAddress ip;
		Socket conSocket;
		Peer test;
		try {
			serverSocket = new ServerSocket(8767);
			conSocket = serverSocket.accept();
			test = new Peer(); 
			
			
			if (args.length <0) {
				System.out.println("Please enter a command");
			} else {
				/*
				 * To start a network , first node must be initialized as a bootstrap node by using the following command.
				 * --boot [Integer Identifier 232]
				 */
				if (args[0].equals("--boot")) {
					nodeID = (Integer.valueOf(args[1]));
					test.init(conSocket, nodeID);} 
				/*
				 * A node wishing to join the network must have the ip address of a node presently known to be connected 
				 * to the network and the id of this node
				 * --bootstrap [IP Address] --id [Integer Identifier 232]
				 */
				
				else if (args[0].equals("--bootstrap")) {
					ip = InetAddress.getByName(args[1]);
					nodeID = (Integer.valueOf(args[2]));
					test.init(conSocket, nodeID);
					test.joinNetwork(new InetSocketAddress(ip, 8767));
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
