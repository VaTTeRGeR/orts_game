package de.vatterger.engine.handler.network;

import java.io.IOException;

import com.esotericsoftware.kryonet.Server;

public class ServerNetworkChannel {
	protected Server server;
	public ServerNetworkChannel(int port, int writeBufferSize, PacketRegister packetRegister) {
		server = new Server(writeBufferSize, 1536);
		packetRegister.register(server.getKryo());
		try {
			server.start();
			server.bind(port, port);
		} catch (IOException e) {
			e.printStackTrace();
			server = null;
		}
	}
}
