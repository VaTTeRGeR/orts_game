package de.vatterger.tests;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import de.vatterger.engine.network.layered.RUDPQueue;

public class RUDPQueueServerTest {

	public static void main(String[] args) throws Exception {
		
		InetSocketAddress a0 = new InetSocketAddress(26000);

		RUDPQueue q0 = new RUDPQueue(a0); //200 Mbit
		
		q0.setDataRateMax(45*1024); //45kbyte/s max
		
		q0.bind();
		
		while(true) {
			
			DatagramPacket p = q0.read();
			
			if(p != null) {
				
				System.out.println(new String(p.getData(),"utf-8"));
				
				q0.write((InetSocketAddress)p.getSocketAddress(), p.getData(), true);
			}
			
			Thread.yield();
		}
	}
}