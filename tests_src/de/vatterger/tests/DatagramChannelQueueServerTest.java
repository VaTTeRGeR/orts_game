package de.vatterger.tests;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import de.vatterger.engine.network.layered.DatagramChannelQueue;

public class DatagramChannelQueueServerTest {

	public static void main(String[] args) throws Exception {
		
		InetSocketAddress a0 = new InetSocketAddress("localhost", 26000);
		InetSocketAddress a1 = new InetSocketAddress("localhost", 27000);

		DatagramChannelQueue q0 = new DatagramChannelQueue(a0, 50*1024*1024/8);
		
		q0.bind();
		
		int counter = 0;
		long t_since = System.currentTimeMillis();
		while(true) {
			DatagramPacket p = null;
			while((p = q0.read()) != null) {
				q0.write(p.getSocketAddress(), p.getData());
				counter++;
			}
			
			if(System.currentTimeMillis() - t_since > 1000) {
				System.out.println("packets: " + counter);
				t_since = System.currentTimeMillis();
			}
			
			Thread.sleep(5);
		}
	}
}