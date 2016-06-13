package de.vatterger.engine.handler.network;

import java.io.IOException;
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

	private static ServerNetworkHandler service;

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
	private ServerNetworkHandler(PacketRegister register) {
		server = new Server(320000, 1500);
		numConnections = 0;
		Log.set(Log.LEVEL_INFO);

		register.register(server.getKryo());

		try {
			server.start();
			server.bind(26000, 26000);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		server.addListener(new Listener() {
			@Override
			public void connected(Connection c) {
				numConnections++;
				connections.add(c);
				connectedQueue.add(c);
			}

			@Override
			public void disconnected(Connection c) {
				numConnections--;
				connections.remove(c);
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
	 * Returns the NetworkService instance. May be slow on first call!
	 * 
	 * @return Instance of NetworkService
	 */
	public static synchronized ServerNetworkHandler instance(PacketRegister register) {
		if (!loaded())
			return service = new ServerNetworkHandler(register);
		return service;
	}

	/**
	 * Returns the NetworkService instance. May be slow on first call!
	 * 
	 * @return Instance of NetworkService
	 */
	public static synchronized ServerNetworkHandler instance() {
		if (!loaded())
			throw new IllegalStateException("Initialize ServerNetworkHandler first.");
		return service;
	}

	/**
	 * Returns whether the service is already loaded
	 * 
	 * @return true if the service is loaded
	 */
	public static boolean loaded() {
		return service != null;
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
		if (loaded()) {
			try {
				service.server.stop();
				service.server.close();
				//service.threadSend.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				service = null;
			}
		}
	}
}
