package de.vatterger.entitysystem.netservice;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

/**
 * Creates a singleton TCP/UDP Server on port 26000. Stores messages and accepts connections
 * @author Florian Schmickmann
 **/
public class NetworkService {
	
	private static NetworkService service;
	
	/**The KryoNet server*/
	private Server server;
	
	/**Output buffer size in bytes*/
	private static final int QUEUE_BUFFER_SIZE = 1024*1024*16; // 16M
	/**Object graph buffer size in bytes*/
	private static final int OBJECT_BUFFER_SIZE = 1024*1024*4; // 4M
	/**Port to bind TCP and UDP*/
	private static final int NET_PORT = 26000;
	
	/**Received messages are stored in this queue*/
	private Queue<Message> receiveQueue = new ConcurrentLinkedQueue<Message>();

	/**Received messages are stored in this queue*/
	private int numConnections;
	
	/**
	 * Private constructor, use instance to obtain the Service!
	 **/
	private NetworkService() {
		server = new Server(QUEUE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);
		numConnections = 0;
		Log.set(Log.LEVEL_INFO);

		PacketRegister.registerClasses(server.getKryo());

		try {
			server.start();
			server.bind(NET_PORT, NET_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		server.addListener(new Listener(){
			@Override
			public void connected(Connection c) {
				numConnections++;
			}
			
			@Override
			public void disconnected(Connection c) {
				numConnections--;
			}

			@Override
			public void received(Connection c, Object o) {
				receiveQueue.add(new Message(o, c));
			}
		});
	}
	
	/**
	 * Returns the next message from the queue
	 * @return A message or null
	 */
	public Message getNextMessage() {
		return receiveQueue.poll();
	}
	
	/**
	 * Immediately sends a Message via TCP
	 * @param m Message to send
	 */
	public void sendReliable(Message m) {
		server.sendToTCP(m.getConnection().getID(), m.getObject());
	}

	/**
	 * Immediately sends a Message via UDP
	 * @param m Message to send
	 */
	public void sendUnreliable(Message m) {
		server.sendToUDP(m.getConnection().getID(), m.getObject());
	}
		
	/**
	 * Returns/creates the NetworkService instance. May be slow on first call!
	 * @return Instance of NetworkService
	 */
	public static synchronized NetworkService instance() {
		if(!loaded())
			service = new NetworkService();
		return service;
	}
	
	/**
	 * Returns whether the service is already loaded
	 * @return true if it is loaded
	 */
	public static boolean loaded() {
		return service != null;
	}
	
	/**
	 * Returns the number of clients currently connected
	 * @return Number of connections
	 */
	public int getNumConnections() {
		return numConnections;
	}

	/**
	 * Disposes of the Service if loaded, or does nothing if there's no service.
	 */
	public static synchronized void dispose() {
		if (loaded()) {
			try {
				service.server.stop();
				service.server.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			} finally {
				service = null;
			}
		}
	}
}
