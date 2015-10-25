package de.vatterger.entitysystem.netservice;

import static de.vatterger.entitysystem.GameConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.artemis.utils.Bag;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

/**
 * Creates a singleton TCP/UDP Server on port 26000. Stores messages and accepts
 * connections
 * 
 * @author Florian Schmickmann
 **/
public class ClientNetworkService {

	private static ClientNetworkService service;
	
	private static String ADDRESS = NET_SERVER_IP;

	private static int PORT = NET_PORT;

	/** The KryoNet server */
	private Client client;

	/**
	 * Private constructor, use instance to obtain the Service!
	 **/
	private ClientNetworkService() {
		client = new Client(QUEUE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);
		Log.set(NET_LOGLEVEL);

		PacketRegister.registerClasses(client.getKryo());

		try {
			client.start();
			client.connect(NET_CONNECT_TIMEOUT, ADDRESS, PORT, PORT);
		} catch (IOException e) {
			dispose();
			System.exit(1);
		}
	}
	
	public void addListener(Listener listener) {
		client.addListener(listener);
	}
	
	public void removeListener(Listener listener) {
		client.removeListener(listener);
	}
	
	public void send(Object o, boolean reliable) {
		if(reliable)
			client.sendTCP(o);
		else
			client.sendUDP(o);
	}
	
	/**
	 * Sets the goal address/port!
	 * 
	 * @return Instance of NetworkService
	 */
	public synchronized static ClientNetworkService instance(String address, int port){
		ADDRESS = address;
		PORT = port;
		dispose();
		return instance();
	}
	
	/**
	 * Returns/creates the NetworkService instance. May be slow on first call!
	 * 
	 * @return Instance of NetworkService
	 */
	public synchronized static ClientNetworkService instance() {
		if (!loaded())
			service = new ClientNetworkService();
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
	 * Disposes of the Service if loaded, or does nothing if there's no service.
	 */
	public synchronized static void dispose() {
		if (loaded()) {
			try {
				service.client.stop();
				service.client.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				service = null;
			}
		}
	}
}
