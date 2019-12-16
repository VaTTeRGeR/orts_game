package de.vatterger.tests;

import java.net.InetSocketAddress;

import de.vatterger.engine.network.io.SocketQueue;
import de.vatterger.engine.network.io.SocketQueuePacket;

public class TCPQueueClientTest {

	public static void main(String[] args) throws Exception {
		
		//InetSocketAddress a1 = new InetSocketAddress("orts.schmickmann.de", 26000);

		InetSocketAddress a1 = new InetSocketAddress("localhost", 26000);
		
		SocketQueue queue = new SocketQueue();
		
		queue.bind(a1);
		
		while(!queue.isReady()) {
			System.out.println("Not Ready yet...");
			Thread.sleep(1);
		}
		
		System.out.println("Connection to: " + queue.getCurrentAddress());
		
		long tPacketSent = System.nanoTime();
		
		while(queue.isConnected()) {
			
			SocketQueuePacket packet = null;
			
			while((packet = queue.read()) != null) {
				
				System.out.println("Got response after " + (System.nanoTime() - tPacketSent)/1000000 + " ms");

				byte[] data = packet.getByteArray(packet.getInt());
				
				System.out.println(new String(data,"utf-8"));
				
				packet.returnToPacketPool();
			}
		
			packet = queue.getPacketFromPool();
			
			if(packet == null) {
				continue;
			}
			
			byte[] sb = "HELLO FROM CLIENT!".getBytes("utf-8");
			
			packet.putInt(sb.length);
			packet.putByteArray(sb);
			
			
			if(queue.write(packet)) {
				System.out.println("Sending packet to server");
			}
			
			packet = null;
			
			tPacketSent = System.nanoTime();
		
			while((packet = queue.read()) == null);
			
			System.out.println("Got response after " + (System.nanoTime() - tPacketSent)/1000000 + " ms");

			packet.returnToPacketPool();
			
			
			//Thread.yield();
			Thread.sleep(1000);
			
		}
		
		System.out.println("Queue disconnected");
		
		queue.unbind();
	}
}