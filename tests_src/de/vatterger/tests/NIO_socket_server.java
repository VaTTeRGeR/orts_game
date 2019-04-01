package de.vatterger.tests;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class NIO_socket_server {

	public static void main(String[] args) throws IOException, InterruptedException {

		// Selector: multiplexor of SelectableChannel objects
		Selector selector = Selector.open(); // selector is open here

		// ServerSocketChannel: selectable channel for stream-oriented listening
		// sockets
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		InetSocketAddress bindAddress = new InetSocketAddress("localhost", 1111);

		// Binds the channel's socket to a local address and configures the
		// socket to listen for connections
		serverSocketChannel.bind(bindAddress);

		// Adjusts this channel's blocking mode.
		serverSocketChannel.configureBlocking(false);

		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		// Infinite loop..
		// Keep server running
		
		while (true) {
			select(selector, serverSocketChannel);
		}
	}

	private static void select(Selector selector, ServerSocketChannel serverSocketChannel)
			throws IOException, ClosedChannelException {

		int selectedSize = selector.select();
		
		log("selected " + selectedSize + " keys.");
		
		Set<SelectionKey> selectedKeys = selector.selectedKeys();

		for (SelectionKey key : selectedKeys) {
			
			
			if (key.isAcceptable() && key.isValid()) {
				
				try {
					acceptChannel(selector, serverSocketChannel);
				} catch (IOException e) {
					System.out.println("Client accept error.");
				}

			} else if (key.isReadable()) {
				
				SocketChannel client = (SocketChannel) key.channel();

				try {
					
					readChannel(client);
				
					if(key.isValid())
						key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

				} catch (IOException e) {
					System.out.println("Client read error.");
					client.close();
				}

			} else if (key.isWritable() && key.isValid()) {

				SocketChannel client = (SocketChannel) key.channel();
				
				try {

					writeChannel(client);

					if(key.isValid())
						key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
				
				} catch (IOException e) {
					System.out.println("Client write error.");
					client.close();
				}
			}
		}
		selectedKeys.clear();
	}

	private static void acceptChannel(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException, ClosedChannelException {

		SocketChannel client = serverSocketChannel.accept();
		
		// Adjusts this channel's blocking mode to false
		client.configureBlocking(false);

		// Operation-set bit for read operations
		SelectionKey key = client.register(selector, SelectionKey.OP_READ);
		log("Connection Accepted: " + client.getLocalAddress() + " with key " + key.toString() + " \n");
	}

	private static void readChannel(SocketChannel client) throws IOException {
		
		ByteBuffer buffer = ByteBuffer.allocate(64);
		client.read(buffer);
		String result = new String(buffer.array()).trim();

		log("Message received from " + client.getRemoteAddress() + ": " + result);

		if (result.equals("END")) {
			client.close();
			log("\nIt's time to close connection as we got last company name 'END'");
			log("\nServer will keep running. Try running client again to establish new connection");
		}
	}

	private static void writeChannel(SocketChannel client) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(64);
		buffer.put("HELLO CLIENT!".getBytes());
		buffer.flip();
		client.write(buffer);
	}

	private static void log(String str) {
		System.out.println(str);
	}

}
