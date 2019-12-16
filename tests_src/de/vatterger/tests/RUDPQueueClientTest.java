package de.vatterger.tests;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import com.badlogic.gdx.math.MathUtils;

import de.vatterger.engine.network.udp.RUDPQueue;

public class RUDPQueueClientTest {

	public static void main(String[] args) throws Exception {
		
		//InetSocketAddress a0 = new InetSocketAddress(MathUtils.random(30000, 40000));
		//InetSocketAddress a1 = new InetSocketAddress("orts.schmickmann.de", 26000);

		InetSocketAddress a0 = new InetSocketAddress("localhost", MathUtils.random(24000, 40000));
		InetSocketAddress a1 = new InetSocketAddress("localhost", 26000);
		
		RUDPQueue q0 = new RUDPQueue(a0);
		
		q0.bind();
		
		System.out.println("Connected: " + q0.connect(a1, 5000));
		
		//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		while(true) {
			
			DatagramPacket p = q0.read();
			
			if(p != null) {
				System.out.println(new String(p.getData(), "utf-8"));
			}
			
			String s = "HELLO!HELLO!HELLO!HELLO!HELLO!HELLO!HELLO!HELLO!\n";
			
			byte[] sb = s.getBytes("utf-8");
			
			q0.write(a1, sb, true);
			
			Thread.sleep(10);
	    }
		
		//q0.unbind();
	}
}