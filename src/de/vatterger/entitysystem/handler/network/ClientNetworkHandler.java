package de.vatterger.entitysystem.handler.network;

import static de.vatterger.entitysystem.application.GameConstants.*;

import java.io.IOException;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import de.vatterger.entitysystem.network.PacketRegister;

/**
 * Creates a singleton TCP/UDP Server on port 26000. Stores messages and accepts
 * connections
 * 
 * @author Florian Schmickmann
 **/
public class ClientNetworkHandler {

	private static ClientNetworkHandler service;

	private static String ADDRESS0 = LOCAL_SERVER_IP;
	private static String ADDRESS1 = NET_SERVER_IP;
	private static String ADDRESSU = null;

	private static int PORT = NET_PORT;

	/** The KryoNet server */
	private Client client;

	/**
	 * Private constructor, use instance to obtain the Service!
	 **/
	private ClientNetworkHandler() {
		client = new Client(QUEUE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);
		Log.set(NET_LOGLEVEL);

		PacketRegister.registerClasses(client.getKryo());

		client.start();
		try {
			client.connect(NET_CONNECT_TIMEOUT, ADDRESS0, PORT, PORT);
			return;
		} catch (IOException e1) {
			Log.error("Failed to connect to " + ADDRESS0);
		}
		try {
			client.connect(NET_CONNECT_TIMEOUT, ADDRESS1, PORT, PORT);
			return;
		} catch (Exception e2) {
			Log.error("Failed to connect to " + ADDRESS1);
		}
		try {
			client.connect(NET_CONNECT_TIMEOUT, ADDRESSU, PORT, PORT);
			return;
		} catch (Exception e3) {
			Log.error("Failed to connect to " + ADDRESSU);
			dispose();
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
	 * Sets the goal address/port and recreates the ClientNetworkHandler.
	 * 
	 * @return Instance of NetworkService
	 */
	public synchronized static ClientNetworkHandler instance(String address, int port){
		ADDRESSU = address;
		PORT = port;
		dispose();
		return instance();
	}
	
	/**
	 * Returns / creates the NetworkService instance. May be slow on first call!
	 * 
	 * @return Instance of NetworkService
	 */
	public synchronized static ClientNetworkHandler instance() {
		if (!loaded())
			service = new ClientNetworkHandler();
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
