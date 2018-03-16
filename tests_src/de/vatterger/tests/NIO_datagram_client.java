package de.vatterger.tests;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @author Crunchify.com
 *
 */

public class NIO_datagram_client {

	public static void main(String[] args) throws InterruptedException, IOException {

		InetSocketAddress addressBind = new InetSocketAddress("192.168.2.192", 27000);
		InetSocketAddress addressSend = new InetSocketAddress("192.168.2.200", 26000);
		
		DatagramChannel datagramChannel = DatagramChannel.open();

		datagramChannel.setOption(StandardSocketOptions.SO_RCVBUF, 25*1024*1024); //1MByte
		datagramChannel.setOption(StandardSocketOptions.SO_SNDBUF, 25*1024*1024); //1MByte

		datagramChannel.configureBlocking(false);
		
		datagramChannel.bind(addressBind);
		datagramChannel.connect(addressSend);

		while(true) {
			
			byte[] message = new byte[1500];
			ByteBuffer buffer = ByteBuffer.wrap(message);

			buffer.clear();

			int bytesWritten;
			try {
				bytesWritten = datagramChannel.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
				bytesWritten = 0;
			}
			
			//log("bytes written: " + bytesWritten);

			// wait for 2 seconds before sending next message
			//final long INTERVAL = 1 * 1000 * 1000;
			//long start = System.nanoTime();
		    //while(System.nanoTime() - start <= INTERVAL){}
			
			//ByteBuffer bufferRead = ByteBuffer.allocate(256);
			/*try {
				if(datagramChannel.receive(bufferRead) != null) {
					log(new String(bufferRead.array()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}*/			
		}
	}

	private static void log(String str) {
		System.out.println(str);
	}
}