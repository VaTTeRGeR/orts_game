package de.vatterger.tests;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.badlogic.gdx.math.MathUtils;

import de.vatterger.engine.network.layered.DatagramChannelQueue;

public class DatagramChannelQueueClientTest {

	public static void main(String[] args) throws Exception {
		
		InetSocketAddress a0 = new InetSocketAddress("localhost", 27000);
		InetSocketAddress a1 = new InetSocketAddress("localhost", 26000);

		DatagramChannelQueue q0 = new DatagramChannelQueue(a0, 1000*1024*1024/8);
		
		q0.bind();

		while(true) {
			
			for (int j = 0; j < 10; j++) {
				for (int i = 0; i < 500; i++) {
					q0.write(a1, new byte[1024]);
				}
				Thread.sleep(100);
				while(q0.read() != null);
			}
			System.out.println("still going lel");

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