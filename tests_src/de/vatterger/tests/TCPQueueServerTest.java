package de.vatterger.tests;

import java.net.InetSocketAddress;

import de.vatterger.engine.network.io.ServerSocketQueue;
import de.vatterger.engine.network.io.SocketQueue;
import de.vatterger.engine.network.io.SocketQueuePacket;

public class TCPQueueServerTest {

	public static void main(String[] args) throws Exception {
		
		InetSocketAddress a0 = new InetSocketAddress(26000);

		ServerSocketQueue socketQueue = new ServerSocketQueue();
		
		socketQueue.bind(a0);
		
		while(true) {
			
			SocketQueue queue = socketQueue.pollAcceptedSocketQueue();
			
			if(queue != null) {
				
				System.out.println("New Connection: " + queue.getCurrentAddress());
				
				while(queue.isReady()) {
					
					SocketQueuePacket packet = null;
					
					while((packet = queue.read()) != null) {
						
						//System.out.println("Got request from Client.");
						
						packet.returnToPacketPool();
						
						packet = null;
						
						while((packet = queue.getPacketFromPool()) == null);
						
						byte[] sb = "RESPONSE FROM SERVER!dwadawdawd dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddawaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaawlrfawoirj3894zh2308h444444444444444444444444444444444444hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhaaaaa".getBytes("utf-8");
						
						packet.putInt(sb.length);
						packet.putByteArray(sb);
						
						queue.write(packet);
					}

					//Thread.yield();
					Thread.sleep(1);
				}
			}
			
			queue = socketQueue.pollStoppedSocketQueue();
			
			if(queue != null) {
				System.out.println("Stopped: " + queue.getCurrentAddress());
			}
		}
	}
}