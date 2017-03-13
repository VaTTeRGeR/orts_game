package de.vatterger.engine.handler.network;

import java.io.IOException;
import java.util.HashMap;

public class ServerNetworkChannelHandler {
	
	private static HashMap<Integer, ServerNetworkChannel> CHANNELS = new HashMap<Integer, ServerNetworkChannel>();
	
	public static void createChannel(int port, int writeBufferSize, PacketRegister packetRegister) {
		if(!isChannelLoaded(port)) {
			ServerNetworkChannel channel = new ServerNetworkChannel(port, writeBufferSize, packetRegister);
			CHANNELS.put(port, channel);
		} else {
			System.err.println("Channel "+port+" is already initialized!");
		}
	}
	
	public static boolean isChannelLoaded(int port) {
		return CHANNELS.containsKey(port);
	}
	
	public static ServerNetworkChannel getChannel(int port) {
		ServerNetworkChannel channel = CHANNELS.get(port);
		return channel;
	}
	
	public static void closeChannel(int port) {
		if(isChannelLoaded(port)) {
			try {
				CHANNELS.get(port).server.dispose();
				CHANNELS.remove(port);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
