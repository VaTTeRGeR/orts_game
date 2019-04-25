package de.vatterger.engine.network.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import com.badlogic.gdx.math.MathUtils;

/**
 * Highly performant and asynchronous message passing built on TCP Sockets. Handles connecting and disconnecting. Allows payloads of up to 4096 byte per SocketQueuePacket.
 * @see ServerSocketQueue
 * @see SocketQueuePacket
 */
public class SocketQueue {

	private static final int BUFFER_SIZE = 4096;

	/**milliseconds*/
	protected static final long THREAD_START_TIMEOUT = 5000;
	
	private Thread readThread = null;
	private Thread writeThread = null;
	
	private ArrayBlockingQueue<SocketQueuePacket> packetPoolQueue = new ArrayBlockingQueue<>(64, false);
	
	private ArrayBlockingQueue<SocketQueuePacket> receiveQueue = new ArrayBlockingQueue<>(256, false);
	private ArrayBlockingQueue<SocketQueuePacket> sendQueue = new ArrayBlockingQueue<>(256, false);
	
	private volatile boolean isConnected = false;
	private volatile boolean isBound = false;

	private volatile OutputStream outputStream;
	private volatile InputStream inputStream;
	
	
	public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException, IOException {
		
		byte[] payload = ("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam "
				+ "nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, "
				+ "sed diam voluptua. At vero eos et accusam et justo duo dolores et ea "
				+ "rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum "
				+ "dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
				+ "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam "
				+ "erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea "
				+ "rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum "
				+ "dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed"
				+ " diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,"
				+ " sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum."
				+ " Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. \r\n" + 
				"\r\n" + 
				"Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie "
				+ "consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto"
				+ "odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla"
				+ " facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod "
				+ "tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
				+ "exerci tation ullamcorper suscipit lobortis nislee").getBytes("utf-8");
		
		for (int n = 0; n < 10; n++) {
	
			new Thread( () -> {
				
				InetSocketAddress address = new InetSocketAddress("localhost", 26000);
				//InetSocketAddress address = new InetSocketAddress("schmickmann.de", 26000);
				
				SocketQueue queue = new SocketQueue();
				
				queue.bind(address);
				
				while(!queue.isReady()) {
					Thread.yield();
					System.out.println("Waiting for isConnected()...");
				}
				System.out.println("...connected!");
				
				long sumBytes = 0;
				long tByteCountBegin = System.currentTimeMillis() - 1;
				
				while (queue.isConnected()) {
		
					for (int i = 0; i < 5; i++) {
		
						SocketQueuePacket packet = queue.getPacketFromPool();
						
						packet.buffer.put(payload);
						
						sumBytes+= payload.length;
						
						long tStart = System.nanoTime();
						
						queue.write(packet);
						
						long tDelta = System.nanoTime() - tStart;
						
						//System.out.println("Queueing time: " + TimeUnit.NANOSECONDS.toMicros(tDelta) + " us");
						
					}
		
					if(MathUtils.randomBoolean(0.01f))
						System.out.println("Kilobyte/s: " + (sumBytes * 1000 / 1024 / (System.currentTimeMillis() - tByteCountBegin)));
					
					SocketQueuePacket packetReceived = null;
					
					while((packetReceived = queue.read()) != null) {
						queue.returnPacketToPool(packetReceived);
					}
					
					//System.out.println(new String(response, 0, bytesRead));
					
					//Thread.yield();
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				queue.stop();
				
				System.out.println("Queue stopped: " + !queue.isReady());
			}).start();
			
			Thread.sleep(100);
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

						socket.connect(addressConnect);
						
						isConnected = true;
					}
					
					System.out.println("SO_SNDBUF: " + socket.getSendBufferSize() + " Byte");
					System.out.println("SO_RCVBUF: " + socket.getReceiveBufferSize() + " Byte");
					
					System.out.println("New Connection: " + socket.getRemoteSocketAddress());
					
					outputStream = socket.getOutputStream();
					inputStream = socket.getInputStream();
					
					final InputStream in = socket.getInputStream();
					
					while(!Thread.interrupted() && writeThread.isAlive()) {
						
						try {
							
							SocketQueuePacket packet = getPacketFromPool();
							
							packet.readFromInputStream(in);
							
							receiveQueue.put(packet);
							
						} catch (Exception e) {
							isConnected = false;
							Thread.currentThread().interrupt();
						}
					}
					
				} catch (Exception e) {
					isConnected = false;
					e.printStackTrace();
				}
			}
		};
		
		readThread = new Thread(readPacketRunnable, "Socket-Read-Thread");
		
		readThread.setDaemon(true);

		readThread.start();

		
		Runnable writePacketRunnable = new Runnable() {
			
			@Override
			public void run() {
				
				final long tStart = System.currentTimeMillis();
				
				while(outputStream == null && System.currentTimeMillis() - tStart < THREAD_START_TIMEOUT) {
					
					Thread.yield();
					
					if(!readThread.isAlive()) {
						
						isConnected = false;
						
						Thread.currentThread().interrupt();

						break;
					}
				}
				
				final OutputStream out = outputStream;

				while(!Thread.interrupted() && readThread.isAlive()) {

					try {
						
						SocketQueuePacket packet = sendQueue.take();
						
						if(packet != null) {
							
							packet.writeToOutputStream(out);
							
							returnPacketToPool(packet);
						}
						
					} catch (Exception e) {
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
		return receiveQueue.poll();
	}
	
	/**
	 * Sends this SocketQueuePacket off to the thread that writes the SocketQueuePackets.
	 * @param packet The SocketQueuePacket to send to the other side. Do not flip the buffer yourself.
	 * @return True if the packet was successfully enqueued for sending, false if the send queue is full.
	 */
	public boolean write(SocketQueuePacket packet) {
		
		if(packet.getOrigin() == this) {
			return sendQueue.offer(packet);
		} else {
			throw new IllegalArgumentException("Do not use packets from other SocketQueues. Each SocketQueue maintains its own packet-pool.");
		}
	}
	
	/**
	 * Fetches a SocketQueuePacket from the packet-pool or creates a new one if the packet-pool is empty.
	 * @return A SocketQueuePacket ready to be used for writing data to.
	 */
	public SocketQueuePacket getPacketFromPool() {

		SocketQueuePacket packet = packetPoolQueue.poll();
		
		if(packet == null) {
			packet = new SocketQueuePacket(BUFFER_SIZE, this);
		} else {
			packet.buffer.clear();
		}
		
		return packet;
	}
	
	/**
	 * Returns the SocketQueuePacket to the SocketQueuePacket-pool to reuse it.
	 */
	protected void returnPacketToPool(SocketQueuePacket packet) {
		packetPoolQueue.offer(packet);
	}
	
	/**
	 * Once the SocketQueue is connected to it's entpoint data can be exchanged.
	 * @return True if connected, otherwise false.
	 */
	public boolean isConnected() {
		return isConnected;
	}
	
	/**
	 * The SocketQueue is bound after calling the bind method, you can already read and write messages but they are only being sent once isConnected() returns true.
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
	public void stop() {

		if(readThread != null) {

			try {
				if(inputStream != null) {
					inputStream.close();
					inputStream = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			readThread.interrupt();
			try {
				readThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if(writeThread != null) {

			try {
				if(outputStream != null) {
					outputStream.close();
					outputStream = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			writeThread.interrupt();
			
			try {
				writeThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		packetPoolQueue.clear();
		
		receiveQueue.clear();
		sendQueue.clear();
		
		isBound = isConnected = false;
	}
}
