package de.vatterger.tests;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NIO_datagram_server {

	public static void main(String[] args) throws IOException, InterruptedException {

		DatagramChannel datagramChannel = DatagramChannel.open();
		InetSocketAddress address = new InetSocketAddress("localhost", 26000);

		datagramChannel.bind(address);
		
		datagramChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024*1024); //1MByte
		datagramChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024*1024); //1MByte
		
		log("SO_RCVBUF= "+datagramChannel.getOption(StandardSocketOptions.SO_RCVBUF));
		log("SO_SNDBUF= "+datagramChannel.getOption(StandardSocketOptions.SO_SNDBUF));

		datagramChannel.configureBlocking(false);

		int counter = 0;

		// Infinite loop..
		// Keep server running
		while (true) {

			ByteBuffer crunchifyBuffer = ByteBuffer.allocate(256);
			InetSocketAddress recvAddress = (InetSocketAddress) datagramChannel.receive(crunchifyBuffer);
			
			if(recvAddress != null) {
				counter++;
				log("Receiving message " + (counter));
				
				String result = new String(crunchifyBuffer.array()).trim();
				log("Message received: " + result + " from " + recvAddress.toString());

				int sentbytes = datagramChannel.send(ByteBuffer.wrap("ok".getBytes()), recvAddress);
				log("Bytes sent: " + sentbytes);
			}
			
			log("Sleeping.");
			Thread.sleep(1000);
		}
	}

	private static void log(String str) {
		System.out.println(str);
	}

}
