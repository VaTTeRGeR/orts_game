package de.vatterger.entitysystem.netservice;

import static de.vatterger.entitysystem.util.Constants.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.LockSupport;

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
public class NetworkService {

	private static NetworkService service;

	/** The KryoNet server */
	private Server server;

	/** Received messages are stored in this queue */
	private Queue<MessageIn> receiveQueue = new ConcurrentLinkedQueue<MessageIn>();

	/** To be sent messages are stored in this queue */
	private BlockingQueue<MessageOut> sendQueue = new LinkedBlockingQueue<MessageOut>();

	/** Received messages are stored in this queue */
	private Queue<Connection> connectedQueue = new ConcurrentLinkedQueue<Connection>();

	/** Received messages are stored in this queue */
	private Queue<Connection> disconnectedQueue = new ConcurrentLinkedQueue<Connection>();

	private Thread threadSend;

	/** Number of active connections */
	private int numConnections;

	/** Number of active connections */
	private Bag<Connection> connections = new Bag<Connection>();

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

			@Override
			public void received(Connection c, Object o) {
				receiveQueue.add(new MessageIn(o, c));
			}
		});
		
		threadSend = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						for (int i = 0; i < 5; i++) {
							sendQueue.take().send();
						}
						Thread.sleep(1);
					}
				} catch (InterruptedException e) {
					System.out.println("Stopped Netservice-send-thread!");
					return;
				}
			}
		}, "Message-send-thread");
		threadSend.start();
	}

	/**
	 * Returns the next message from the queue
	 * 
	 * @return A message or null
	 */
	public MessageIn getNextMessage() {
		return receiveQueue.poll();
	}

	/**
	 * Returns the next message from the queue
	 * 
	 * @return A message or null
	 */
	public void sendMessage(MessageOut m) {
		sendQueue.add(m);
	}

	/**
	 * Returns the next message from the queue
	 * 
	 * @return A message or null
	 */
	public Connection getConnected() {
		return connectedQueue.poll();
	}

	/**
	 * Returns the next message from the queue
	 * 
	 * @return A message or null
	 */
	public Connection getDisconnected() {
		return disconnectedQueue.poll();
	}

	/**
	 * Returns a Bag of active Connections
	 * 
	 * @return Instance of NetworkService
	 */
	public Bag<Connection> getConnections() {
		return connections;
	}

	/**
	 * Returns/creates the NetworkService instance. May be slow on first call!
	 * 
	 * @return Instance of NetworkService
	 */
	public static synchronized NetworkService instance() {
		if (!loaded())
			service = new NetworkService();
		return service;
	}

	/**
	 * Returns whether the service is already loaded
	 * 
	 * @return true if it is loaded
	 */
	public static boolean loaded() {
		return service != null;
	}

	/**
	 * Returns the number of clients currently connected
	 * 
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
				service.threadSend.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				service = null;
			}
		}
	}
}
