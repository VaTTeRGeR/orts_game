package de.vatterger.tests;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
 
/**
 * @author Crunchify.com
 *
 */
 
public class NIO_socket_client {
 
	public static void main(String[] args) throws IOException, InterruptedException {
 
		InetSocketAddress crunchifyAddr = new InetSocketAddress("localhost", 26000);
		SocketChannel crunchifyClient = SocketChannel.open(crunchifyAddr);
  
		ArrayList<String> companyDetails = new ArrayList<String>();
 
		// create a ArrayList with companyName list
		companyDetails.add("Facebook");
		companyDetails.add("Twitter");
		companyDetails.add("IBM");
		companyDetails.add("Google");
		companyDetails.add("END");
 
		for (String companyName : companyDetails) {
 
			byte[] message = new String(companyName).getBytes();
			ByteBuffer buffer = ByteBuffer.wrap(message);
			crunchifyClient.write(buffer);
 
			log("sending: " + companyName);
			buffer.clear();
			
			buffer = ByteBuffer.allocate(64);
			
			int readBytes = crunchifyClient.read(buffer);
			
			if(readBytes > 0) {
				log("Received: " + new String(buffer.array()));
			}
			
			buffer.clear();
 
			// wait for 2 seconds before sending next message
			Thread.sleep(2000);
		}
		
		crunchifyClient.close();
	}
 
	private static void log(String str) {
		System.out.println(str);
	}
}