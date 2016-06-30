package de.vatterger.engine.handler.network;

import com.artemis.utils.Bag;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

/**
 * Creates a singleton TCP/UDP Server on port 26000. Stores messages and accepts
 * connections
 * 
 * @author Florian Schmickmann
 **/
public class ClientNetworkHandler {

	private static ClientNetworkHandler service;

	private static String ADDRESS0 = "localhost";
	private static String ADDRESS1 = "46.101.156.87";
	private static String ADDRESSU = null;

	private static int PORT = 26000;
	
	private static Bag<Listener> listeners = new Bag<Listener>(16);
	private static PacketRegister packetRegister = null;

	/** The KryoNet server */
	private Client client = null;

	/**
	 * Private constructor, use instance to obtain the Service!
	 **/
	private ClientNetworkHandler(PacketRegister packetRegister) {
		client = new Client(32000, 1500);
		Log.set(Log.LEVEL_INFO);

		ClientNetworkHandler.packetRegister = packetRegister;
		ClientNetworkHandler.packetRegister.register(client.getKryo());

		client.start();
		
		if(ADDRESSU == null) {
			try {
				client.connect(500, ADDRESSU = ADDRESS0, PORT, PORT);
				return;
			} catch (Exception e) {
				Log.error("Failed to connect to " + ADDRESSU);
			}
			try {
				client.connect(1000, ADDRESSU = ADDRESS1, PORT, PORT);
				return;
			} catch (Exception e) {
				Log.error("Failed to connect to " + ADDRESSU);
			}
		} else {
			try {
				client.connect(1000, ADDRESSU, PORT, PORT);
				return;
			} catch (Exception e) {
				Log.error("Failed to connect to " + ADDRESSU);
			}
		}
		dispose();
	}
	
	public void addListener(Listener listener) {
		listeners.add(listener);
		client.addListener(listener);
	}
	
	public void removeListener(Listener listener) {
		listeners.remove(listener);
		client.removeListener(listener);
	}
	
	public int send(Object o, boolean reliable) {
		if(reliable)
			return client.sendTCP(o);
		else
			return client.sendUDP(o);
	}
	
	/**
	 * Sets the goal address/port and recreates the ClientNetworkHandler.
	 * 
	 * @return Instance of NetworkService
	 */
	public synchronized static ClientNetworkHandler instance(String address, int port, PacketRegister packetRegister){
		ADDRESSU = address;
		PORT = port;
		dispose();
		return instance(packetRegister);
	}
	
	/**
	 * Returns / creates the NetworkService instance. May be slow on first call!
	 * 
	 * @return Instance of NetworkService
	 */
	public synchronized static ClientNetworkHandler instance(PacketRegister packetRegister) {
		if (!loaded())
			service = new ClientNetworkHandler(packetRegister);
		return service;
	}

	/**
	 * Returns / creates the NetworkService instance. May be slow on first call!
	 * 
	 * @return Instance of NetworkService
	 */
	public synchronized static ClientNetworkHandler get() {
		if (!loaded())
			throw new IllegalStateException("Initialize ClientNetworkHandler first!");
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
	 * Reconnects the NetworkHandler if a previous Connection existed.
	 */
	public static void reconnect(boolean keepListeners) {
		if(packetRegister == null)
			throw new IllegalStateException("Reconnect does not work without a packetregister");
		if(ADDRESSU == null)
			throw new IllegalStateException("Reconnect does not work without a previous connection");
		
		dispose();
		
		service = new ClientNetworkHandler(packetRegister);
		
		if(keepListeners) {
			for (int i = 0; i < listeners.size(); i++) {
				service.client.addListener(listeners.get(i));
			}
		} else {
			listeners.clear();
		}
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
