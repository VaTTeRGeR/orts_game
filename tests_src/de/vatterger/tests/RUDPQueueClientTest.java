package de.vatterger.tests;

import java.net.InetSocketAddress;

import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.engine.network.layered.RUDPQueue;

public class RUDPQueueClientTest {

	public static void main(String[] args) throws InterruptedException {
		
		InetSocketAddress a0 = new InetSocketAddress("localhost", MathUtils.random(24000, 40000));
		InetSocketAddress a1 = new InetSocketAddress("localhost", 26000);
		
		RUDPQueue q0 = new RUDPQueue(a0);
		
		q0.bind();
		
		System.out.println("Connected: " + q0.connect(a1, 1000));
				
		for (int i = 0; i < 1000; i++) {
			Output out = new Output(1400);
			out.writeInt(i);
			out.close();
			
			while(!q0.write(a1, out.getBuffer(), true)){
				System.out.println("STALL");
				Thread.sleep(10);
			}
			Thread.sleep(10);
			
			System.out.println("p:" + i + " @ " +q0.getBytesPerSecond()/1024 + "kb/s");
			
			q0.read();
		}
		
		Thread.sleep(10000);
		
		q0.unbind();
	}
}