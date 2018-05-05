package de.vatterger.tests;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Arrays;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.engine.network.layered.RUDPQueue;

public class RUDPQueueServerTest {

	public static void main(String[] args) throws Exception {
		
		int[] register = new int[1000];
		
		InetSocketAddress a0 = new InetSocketAddress("localhost", 26000);

		RUDPQueue q0 = new RUDPQueue(a0); //200 Mbit
		
		q0.bind();
		
		while(true) {
			DatagramPacket packet;
			while((packet = q0.read()) != null){
				Input in = new Input(packet.getData());

				int recv_int = in.readInt();
				System.out.println(recv_int);
				register[recv_int]++;
				
				in.close();
			}
			Thread.sleep(100);
			
			int d = 0;
			
			for (int i = 0; i < register.length; i++) {
				if(register[i] == 0) {
					//System.out.print("first unACKed: " + i);
					d=i;
					break;
				} else if(i == register.length-1) {
					System.out.println(Arrays.toString(register));
					
					System.out.println("YEEEEEEEEEEEEEE");
					System.out.println("YEEEEEEEEEEEEEE");
					System.out.println("YEEEEEEEEEEEEEE");
					System.out.println("YEEEEEEEEEEEEEE");
					System.exit(0);
				}
			}
			for (int i = register.length-1; i >= 0; i--) {
				if(register[i] > 0) {
					//System.out.println(", last ACKed: " + i + ", d=" + Math.max(0,i-d));
					break;
				} else if(i == 0) {
					//System.out.println();
				}
			}
		}
	}
}