package de.vatterger.tests;

import java.net.InetSocketAddress;

import de.vatterger.engine.network.udp.DatagramChannelQueue;

public class DatagramChannelQueueClientTest {

	public static void main(String[] args) throws Exception {
		
		InetSocketAddress a0 = new InetSocketAddress("localhost", 27000);
		InetSocketAddress b0 = new InetSocketAddress("localhost", 27001);
		InetSocketAddress a1 = new InetSocketAddress("localhost", 26000);
		
		DatagramChannelQueue q0 = new DatagramChannelQueue(a0, 125*1024*1024); //1 Gbit/s
		DatagramChannelQueue q1 = new DatagramChannelQueue(b0, 125*1024*1024); //1 Gbit/s
		
		q0.bind();
		q1.bind();
		
		while(true) {
			
			for (int j = 0; j < 10; j++) {
				for (int i = 0; i < 1000; i++) {
					q0.write(a1, new byte[1024]);
					q1.write(a1, new byte[1024]);
				}
				Thread.sleep(10);
				while(q0.read() != null);
			}
			
			System.out.println("Load: " + (int)(q0.getLoadPercentage()*100f) + " / " + (int)(q1.getLoadPercentage()*100f));
			System.out.println("kB/s: " + (int)(q0.getBytesPerSecond()/1024) + " / " + (int)(q1.getBytesPerSecond()/1024));
			
			/*long time = System.nanoTime();
			
			q0.write(a1, new byte[32]);

			DatagramPacket p = null;
			while((p = q0.read()) == null) {
				if(System.nanoTime() - time > 500*1000*1000) {
					System.out.println("RTT: Time out!");
					break;
				}
				Thread.sleep(1);
			}
			
			if(p != null) {
				System.out.println("RTT: " + (System.nanoTime()-time)/1000000 + "ms");
				Thread.sleep(1000 - (System.nanoTime()-time)/1000000);
			} else {
				Thread.sleep(500);
			}*/
			
			
		}
	}
}