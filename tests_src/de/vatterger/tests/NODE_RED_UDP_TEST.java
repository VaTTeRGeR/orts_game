package de.vatterger.tests;

import java.net.InetSocketAddress;

import com.esotericsoftware.kryo.io.Output;

import de.vatterger.engine.network.udp.DatagramChannelQueue;


public class NODE_RED_UDP_TEST {

	public static void main(String[] args) throws InterruptedException {
		
		InetSocketAddress a0 = new InetSocketAddress("localhost", 26000);
		InetSocketAddress a1 = new InetSocketAddress("localhost", 26018);
		
		DatagramChannelQueue q0 = new DatagramChannelQueue(a0);
		
		q0.bind();
		
		Output out = new Output(64);
		out.writeBytes("Hello NODE RED!".getBytes());;
		out.close();

		for (int i = 0; i < 10; i++) {
			q0.write(a1, out.toBytes());
			
			Thread.sleep(100);
		}
		
		q0.unbind();
	}
}