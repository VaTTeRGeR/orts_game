package de.vatterger.entitysystem.netservice;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

public class NetworkService {
	private static NetworkService service;
	
	/**The KryoNet server*/
	private Server server;
	
	/**Output buffer size in bytes*/
	static final int QUEUE_BUFFER_SIZE = 4194304; // 4M
	/**Object graph buffer size in bytes*/
	static final int OBJECT_BUFFER_SIZE = 1048576; // 1M
	/**Port to bind TCP and UDP*/
	static final int NET_PORT = 26000;
	
	private Queue<Message> receiveQueue = new ConcurrentLinkedQueue<Message>();

	/**
	 * Private Constructor, use obtain to get the Service!
	 */
	private NetworkService() {
		server = new Server(QUEUE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);

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
			}
			
			@Override
			public void disconnected(Connection c) {
			}

			@Override
			public void received(Connection c, Object o) {
				receiveQueue.add(new Message(o, c));
			}
		});
		
		service = this;
	}
	
	/**
	 * Returns the next message in the Queue or null
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
	 * Returns a NetworkService instance
	 */
	public static NetworkService obtain() {
		if(loaded())
			return service;
		else {
			return new NetworkService();
		}
	}
	
	/**
	 * Returns whether the Service is already loaded
	 */
	public static boolean loaded() {
		return service != null;
	}
	
	/**
	 * Disposes of the Service if loaded, or does nothing if there's no service.
	 */
	public static void dispose() {
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
