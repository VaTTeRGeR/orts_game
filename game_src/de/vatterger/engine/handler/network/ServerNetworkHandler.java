package de.vatterger.engine.handler.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.artemis.utils.Bag;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

/**
 * Creates a singleton TCP/UDP Server on port 26000. Stores messages and accepts
 * connections
 * 
 * @author Florian Schmickmann
 **/
public class ServerNetworkHandler {

	private static HashMap<Integer, ServerNetworkHandler> services = new HashMap<Integer, ServerNetworkHandler>(4);

	/** The KryoNet server */
	private Server server;

	/** New Connections are in this queue*/
	private Queue<Connection> connectedQueue = new ConcurrentLinkedQueue<Connection>();

	/** Canceled connections are in this queue */
	private Queue<Connection> disconnectedQueue = new ConcurrentLinkedQueue<Connection>();
	
	/** Number of active connections */
	private int numConnections;

	/** Active connections */
	private Bag<Connection> connections = new Bag<Connection>();

	/**
	 * Private constructor, use instance to obtain the Service!
	 **/
	private ServerNetworkHandler(PacketRegister register, int port) {
		server = new Server(64000, 1536);
		numConnections = 0;
		Log.set(Log.LEVEL_INFO);

		register.register(server.getKryo());

		try {
			server.start();
			server.bind(port, port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		server.addListener(new Listener() {
			@Override
			public void connected(Connection c) {
				numConnections++;
				connections.set(c.getID(), c);
				connectedQueue.add(c);
			}
			
			@Override
			public void disconnected(Connection c) {
				numConnections--;
				connections.remove(c.getID());
				disconnectedQueue.add(c);
			}
		});
	}
	
	public void addListener(Listener listener) {
		server.addListener(listener);
	}
	
	public void removeListener(Listener listener) {
		server.removeListener(listener);
	}
	
	/**
	 * Returns and removes the next new connection from the queue
	 * 
	 * @return A new Connection
	 */
	public Connection getNextConnected() {
		return connectedQueue.poll();
	}

	/**
	 * Returns and removes the next canceled connection from the queue
	 * 
	 * @return A canceled connection
	 */
	public Connection getNextDisconnected() {
		return disconnectedQueue.poll();
	}

	/**
	 * Returns a Bag of active Connections
	 * 
	 * @return Bag of active Connections
	 */
	public Bag<Connection> getConnections() {
		return connections;
	}

	/**
	 * Returns a connection by id
	 * 
	 * @return An active Connection or null
	 */
	public Connection getConnection(int id) {
		return connections.get(id);
	}

	/**
	 * Returns the NetworkService instance. May be slow on first call!
	 * 
	 * @return Instance of NetworkService
	 */
	public static synchronized ServerNetworkHandler instance(PacketRegister register, int port) {
		if (!loaded(port))
			services.put(port, new ServerNetworkHandler(register, port));
		return get(port);
	}

	/**
	 * Returns the NetworkService instance.
	 * 
	 * @return Instance of NetworkService
	 */
	public static synchronized ServerNetworkHandler get(int port) {
		if (!loaded(port))
			throw new IllegalStateException("Initialize ServerNetworkHandler first.");
		return services.get(port);
	}

	/**
	 * Returns whether the service is already loaded
	 * 
	 * @return true if the service is loaded
	 */
	public static boolean loaded(int port) {
		return services.get(port) != null;
	}

	/**
	 * Returns the number of clients currently connected
	 * 
	 * @return Number of active connections
	 */
	public int getNumConnections() {
		return numConnections;
	}

	/**
	 * Disposes of the Service if loaded, or does nothing if there's no service.
	 */
	public static synchronized void dispose() {
		for (ServerNetworkHandler snh : services.values()) {
			try {
				snh.server.stop();
				snh.server.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		services.clear();
	}

	/**
	 * Disposes of the Service if loaded, or does nothing if there's no service.
	 */
	public static synchronized void dispose(int port) {
		if (loaded(port)) {
			ServerNetworkHandler snh = get(port);
			try {
				snh.server.stop();
				snh.server.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			services.remove(port);
		}
	}
}
