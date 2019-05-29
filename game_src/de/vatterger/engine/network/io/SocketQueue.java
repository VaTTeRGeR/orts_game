package de.vatterger.engine.network.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Highly performant and asynchronous message passing built on TCP Sockets. Handles connecting and disconnecting. Allows payloads of up to 8192 byte per SocketQueuePacket.
 * @see ServerSocketQueue
 * @see SocketQueuePacket
 */
public class SocketQueue {

	// Buffer sizes greater than 8192 byte will not fit into the local stack for socketWrite0()
	private static final int BUFFER_SIZE = 8192;

	/**milliseconds*/
	protected static final long THREAD_START_TIMEOUT = 5000;
	
	/**milliseconds*/
	protected static final int CONNECT_TIMEOUT = 5000;
	
	private volatile Thread readThread = null;
	private volatile Thread writeThread = null;
	
	private RingBuffer<SocketQueuePacket> packetPoolQueue = new RingBuffer<>(256);
	
	private RingBuffer<SocketQueuePacket> receiveQueue = new RingBuffer<>(256);
	private RingBuffer<SocketQueuePacket> sendQueue = new RingBuffer<>(256);
	
	private volatile boolean isConnected = false;
	private volatile boolean isBound = false;

	private volatile OutputStream outputStream;
	private volatile InputStream inputStream;
	
	
	public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException, IOException {
		
		byte[] payload = ("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam "
				+ "nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, "
				+ "sed diam voluptua. At vero eos et accusam et justo duo dolores et ea "
				+ "rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum "
				+ "dolor sit amet. Lorem ipsum dolor sit amet, conseteetur sadipscing elitr, "
				+ "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam "
				+ "erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea "
				+ "rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum "
				+ "dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed"
				+ " diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,"
				+ " sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum."
				+ " Stet clita kasd gubergren, no sea takimata sanctus este Lorem ipsum dolor sit amet. "
				+ "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie "
				+ "consequat, vel illum dolore eu feugiat nulla facilisis at veros eros et accumsan et iusto"
				+ "odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla"
				+ " facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod "
				+ "tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
				+ "exerci tation ullamcorper suscipit lobortis nislee.").getBytes("utf-8");
		
		for (int n = 0; n < 1; n++) {
	
			new Thread( () -> {
				
				InetSocketAddress address = new InetSocketAddress("localhost", 26000);
				//InetSocketAddress address = new InetSocketAddress("schmickmann.de", 26000);
				
				SocketQueue queue = new SocketQueue();
				
				queue.bind(address);
				
				while(!queue.isReady()) {
					Thread.yield();
				}
				
				System.out.println("...connected!");
				
				long sumBytes = 0;
				long tByteCountBegin = System.currentTimeMillis() - 1;
				
				while (queue.isReady()) {
		
					long tStart = System.nanoTime();

					for (int i = 0; i < 100; i++) {

						SocketQueuePacket packet = queue.getPacketFromPool();
						
						if(packet == null) continue;
						
						packet.putByteArray(payload);
						
						packet.putIntArray(new int[] {1,2,3,42});

						sumBytes += packet.position() - SocketQueuePacket.HEADER_SIZE;
						
						while(!queue.write(packet)) {}
					}
					
					long tDelta = (System.nanoTime() - tStart) / 100;
					
					System.out.println("Send time: " + TimeUnit.NANOSECONDS.toMicros(tDelta) + " us / " + tDelta + " ns");
		
					//System.out.println("Kilobyte/s: " + (sumBytes * 1000 / 1024 / (System.currentTimeMillis() - tByteCountBegin)));
					
					SocketQueuePacket packetReceived = null;
					
					while((packetReceived = queue.read()) != null) {
						packetReceived.returnToPacketPool();
					}
					
					//System.out.println(new String(response, 0, bytesRead));
					
					//Thread.yield();
					
					try {
						Thread.sleep((long)(Math.random()*100));
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
				
				queue.stop();
				
				System.out.println("Queue stopped: " + !queue.isReady());
				
			}).start();
			
			Thread.sleep(10);
		}
	}
	
	/**
	 * Initializes the SocketQueue and connects its internal socket to the specified address.
	 * @param addressConnect The address to connect to.
	 * @return True if binding was successful. This does not mean that the connection is already established.
	 */
	public boolean bind(InetSocketAddress addressConnect) {
		return isBound = bind(addressConnect, null);
	}
	
	/**
	 * Initializes the SocketQueue with the specified socket.
	 * @param reusableSocket The socket to use. Needs to be connected already.
	 * @return True if binding was successful. This does not mean that the connection is already established.
	 */
	public boolean bind(Socket reusableSocket) {
		return isBound = bind(null, reusableSocket);
	}
	
	private boolean bind(InetSocketAddress addressConnect, Socket existingSocket) {
		
		if(isBound() || isConnected()) {
			throw new IllegalStateException("The SocketQueue is already bound or connected. Call stop() before binding again.");
		}
		
		if(!(addressConnect != null ^ existingSocket != null)) {
			throw new IllegalArgumentException("Either addressConnect or socket have to be null.");
		}

		packetPoolQueue.clear();
		
		for (int i = 0; i < packetPoolQueue.capacity(); i++) {
			packetPoolQueue.put(new SocketQueuePacket(BUFFER_SIZE, this));
		}
		
		sendQueue.clear();
		receiveQueue.clear();
		
		if(existingSocket != null) {
			if(existingSocket.isConnected() && !existingSocket.isClosed()) {
				isConnected = true;
			} else {
				return false;
			}
		}
		
		Runnable readPacketRunnable = new Runnable() {
			
			@Override
			public void run() {
				
				try(Socket socket = (existingSocket == null) ? new Socket() : existingSocket;) {
					
					socket.setTcpNoDelay(true);

					socket.setSendBufferSize(1024*64);
					socket.setReceiveBufferSize(1024*64);
					
					if(existingSocket == null) {

						socket.connect(addressConnect, CONNECT_TIMEOUT);
						
						isConnected = true;
					}
					
					System.out.println("SO_SNDBUF: " + socket.getSendBufferSize() + " Byte");
					System.out.println("SO_RCVBUF: " + socket.getReceiveBufferSize() + " Byte");
					
					System.out.println("New Connection: " + socket.getRemoteSocketAddress());
					
					outputStream = socket.getOutputStream();
					inputStream = socket.getInputStream();
					
					final InputStream in = socket.getInputStream();
					
					while(!Thread.currentThread().isInterrupted()) {
						
						try {
							
							SocketQueuePacket packet = getPacketFromPool();
							
							if(packet != null) {

								packet.readFromInputStream(in);
								
								while(!receiveQueue.put(packet)) {
									Thread.sleep(10);
								}
								
							} else {
								Thread.sleep(10);
							}
							
						} catch (Exception e) {
							isConnected = false;
							Thread.currentThread().interrupt();
						}
					}
					
				} catch (Exception e) {
					isConnected = false;
					//e.printStackTrace();
				}
			}
		};
		
		readThread = new Thread(readPacketRunnable, "Socket-Read-Thread");
		
		readThread.setDaemon(true);

		readThread.start();

		
		Runnable writePacketRunnable = new Runnable() {
			
			@Override
			public void run() {
				
				final Thread readThreadLocal = readThread;
				
				if(readThreadLocal == null) {
					isConnected = false;
					return;
				}
				
				while(outputStream == null && !Thread.currentThread().isInterrupted()) {
					
					Thread.yield();
					
					if(!readThreadLocal.isAlive()) {
						isConnected = false;
						Thread.currentThread().interrupt();
						break;
					}
				}
				
				final OutputStream out = outputStream;
				
				while(!Thread.currentThread().isInterrupted() && readThreadLocal.isAlive()) {

					try {
						
						if(sendQueue.has()) {

							SocketQueuePacket packet = sendQueue.get();
							
							packet.writeToOutputStream(out);
							
							packet.returnToPacketPool();
							
						} else {
							Thread.sleep(10);
						}
						
					} catch (InterruptedException e) {
						isConnected = false;
						Thread.currentThread().interrupt();
					} catch (IOException e) {
						isConnected = false;
						Thread.currentThread().interrupt();
					}
				}
			}
		};
		
		writeThread = new Thread(writePacketRunnable, "Socket-Write-Thread");

		writeThread.setDaemon(true);
		
		writeThread.start();
		
		return true;
	}
	
	/**
	 * Polls for a newly received SocketQueuePacket.
	 * @return A SocketQueuePacket if available, otherwise null is returned.
	 */
	public SocketQueuePacket read() {
		return receiveQueue.get();
	}
	
	/**
	 * Sends this SocketQueuePacket off to the thread that writes the SocketQueuePackets.
	 * @param packet The SocketQueuePacket to send to the other side. Do not flip the buffer yourself.
	 * @return True if the packet was successfully enqueued for sending, false if the send queue is full.
	 */
	public boolean write(SocketQueuePacket packet) throws IllegalArgumentException {
		
		if(!packet.isLocked()) {
			
			packet.lock();
			
			if(sendQueue.put(packet)) {

				return true;
				
			} else {
				
				packet.unlock();
				
				return false;
			}
			
		} else {
			throw new IllegalArgumentException("Do not write a packet twice. Always use a fresh packet from the pool.");
		}
	}
	
	/**
	 * Fetches a SocketQueuePacket from the packet-pool or creates a new one if the packet-pool is empty.
	 * @return A SocketQueuePacket ready to be used for writing data to.
	 */
	public SocketQueuePacket getPacketFromPool() {

		SocketQueuePacket packet = packetPoolQueue.get();
		
		if(packet != null) {
			packet.reset();
			packet.unlock();
		}
		
		return packet;
	}
	
	/**
	 * Returns the SocketQueuePacket to the SocketQueuePacket-pool to reuse it.
	 */
	protected boolean returnPacketToPool(SocketQueuePacket packet) {

		packet.lock();
		
		if(packetPoolQueue.put(packet)) {

			return true;

		} else {

			packet.unlock();
			
			return false;
		}
	}
	
	/**
	 * Once the SocketQueue is connected to it's entpoint data can be exchanged.
	 * @return True if connected, otherwise false.
	 */
	public boolean isConnected() {
		return isConnected;
	}
	
	/**
	 * The SocketQueue is bound directly after calling the bind method, you can already read and write messages but they are being sent once isConnected() returns true.
	 * @return True if bound, otherwise false.
	 */
	public boolean isBound() {
		return isBound;
	}
	
	/**
	 * The SocketQueue is ready to use once it is bound and connected.
	 * @return True if ready to use, otherwise false.
	 */
	public boolean isReady() {
		return isConnected() && isBound();
	}
	
	/**
	 * Unbinds and cleans up the SocketQueue.
	 */
	public boolean stop() {

		if(readThread != null && readThread.isAlive()) {

			try {
				if(inputStream != null) {
					inputStream.close();
					inputStream = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
			readThread.interrupt();
			
			try {
				readThread.join();
			} catch (InterruptedException e) {
				return false;
			}

			readThread = null;
		}

		if(writeThread != null && writeThread.isAlive()) {

			try {
				if(outputStream != null) {
					outputStream.close();
					outputStream = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
			writeThread.interrupt();
			
			try {
				writeThread.join();
			} catch (InterruptedException e) {
				return false;
			}
			
			writeThread = null;
		}
		
		
		packetPoolQueue.clear();
		
		receiveQueue.clear();
		sendQueue.clear();
		
		isBound = isConnected = false;
		
		return true;
	}
}
