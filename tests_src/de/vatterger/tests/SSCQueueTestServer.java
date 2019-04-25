package de.vatterger.tests;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import de.vatterger.engine.network.nio.ServerSocketChannelQueue;
import de.vatterger.engine.network.nio.SocketChannelPacket;

public class SSCQueueTestServer {

	public static void main(String[] args) throws InterruptedException, UnknownHostException {
		
		ServerSocketChannelQueue queue = new ServerSocketChannelQueue(new InetSocketAddress("localhost",26000), 5);
		
		queue.bind();
		
		while (true) {
			
			Thread.sleep(500);
			
			SocketChannelPacket packet = queue.grabPacketfromPool();
			
			packet.reset();
			
			packet.getBuffer().put("TEEHEE!".getBytes());
			
			queue.write(packet);
		}
	}

}
