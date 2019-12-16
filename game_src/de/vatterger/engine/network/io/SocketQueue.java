package de.vatterger.engine.network.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import de.vatterger.engine.util.AtomicRingBuffer;

/**
 * Highly performant and asynchronous message passing built on TCP Sockets. Handles connecting and disconnecting. Allows payloads of up to 8192 bytes per SocketQueuePacket.
 * @see ServerSocketQueue
 * @see SocketQueuePacket
 */
public class SocketQueue {

	private final int CONNECT_TIMEOUT;
	
	private final int PACKET_POOL_SIZE;
	
	private final int TCP_RX_BUFFER_SIZE;
	private final int TCP_TX_BUFFER_SIZE;

	// Buffer sizes greater than 8192 byte will not fit into the local stack for socketWrite0()
	private final int PACKET_BUFFER_SIZE;

	private final int WRITE_THREAD_SLEEP_MIN;
	private final int WRITE_THREAD_SLEEP_MAX;
	private final int WRITE_THREAD_SLEEP_GAIN;

	
	private final AtomicRingBuffer<SocketQueuePacket> packetReadPoolQueue;
	private final AtomicRingBuffer<SocketQueuePacket> packetWritePoolQueue;
	
	private final AtomicRingBuffer<SocketQueuePacket> receiveQueue;
	private final AtomicRingBuffer<SocketQueuePacket> sendQueue;
	
	
	private volatile boolean isConnected = false;
	private volatile boolean isBound = false;

	private volatile Thread readThread = null;
	private volatile Thread writeThread = null;
	
	private volatile OutputStream outputStream;
	private volatile InputStream inputStream;
	
	private volatile InetSocketAddress currentAddress = null;
	
	public SocketQueue() {
		this(new SocketQueueConfiguration());
	}
	
	public SocketQueue(SocketQueueConfiguration configuration) {
		
		CONNECT_TIMEOUT = configuration.CONNECT_TIMEOUT;
		
		PACKET_POOL_SIZE = configuration.PACKET_POOL_SIZE;
		
		TCP_RX_BUFFER_SIZE = configuration.TCP_RX_BUFFER_SIZE;
		TCP_TX_BUFFER_SIZE = configuration.TCP_TX_BUFFER_SIZE;
		
		PACKET_BUFFER_SIZE = configuration.PACKET_BUFFER_SIZE;
		
		WRITE_THREAD_SLEEP_MIN = Math.max(0, configuration.WRITE_THREAD_SLEEP_MIN);
		WRITE_THREAD_SLEEP_MAX = Math.max(WRITE_THREAD_SLEEP_MIN, configuration.WRITE_THREAD_SLEEP_MAX);
		WRITE_THREAD_SLEEP_GAIN = Math.max(0, configuration.WRITE_THREAD_SLEEP_GAIN);
		
		packetReadPoolQueue = new AtomicRingBuffer<>(PACKET_POOL_SIZE);
		packetWritePoolQueue = new AtomicRingBuffer<>(PACKET_POOL_SIZE);

		receiveQueue = new AtomicRingBuffer<>(PACKET_POOL_SIZE);
		sendQueue = new AtomicRingBuffer<>(PACKET_POOL_SIZE);
	}
	
	/**
	 * Initializes the SocketQueue and connects its internal socket to the specified address.
	 * @param addressConnect The address to connect to.
	 * @return True if the internal structure was successfully initialized. The connection will be established
	 * in the background. The SocketQueue can send data once isReady() returns true.
	 */
	public boolean bind(InetSocketAddress addressConnect) {
		return isBound = bind(addressConnect, null);
	}
	
	/**
	 * Initializes the SocketQueue with the specified pre-connected socket.
	 * @param connectedSocket The socket to use. Needs to be connected already.
	 * @return True if the binding operation was successful.
	 */
	public boolean bind(Socket connectedSocket) {
		return isBound = bind(null, connectedSocket);
	}
	
	/**
	 * Initializes the SocketQueue with the either the specified address and timeout or an connected socket.
	 * @param reusableSocket The already connected socket to use. Leave addressConnect as null if you use this parameter.
	 * @param addressConnect The address to connect to. Leave reusableSocket as null if you use this parameter.
	 * @param timeout A timeout for the socket connect operation. Only applies when connecting via address.
	 * @return True if binding was successful. This does not mean that the connection is already established.
	 */
	private boolean bind(InetSocketAddress addressConnect, Socket existingSocket) {
		
		currentAddress = null;
		
		if(isBound() || isConnected()) {
			throw new IllegalStateException("The SocketQueue is already bound or connected. Call unbind() before binding again.");
		}
		
		if(!(addressConnect != null ^ existingSocket != null)) {
			throw new IllegalArgumentException("Either addressConnect or existingSocket have to be null.");
		}

		if(CONNECT_TIMEOUT < 0) {
			throw new IllegalArgumentException("Timeout must be >= zero. Supplied timeout: " + CONNECT_TIMEOUT);
		}
		
		packetReadPoolQueue.clear();
		
		for (int i = 0; i < packetReadPoolQueue.capacity(); i++) {
			packetReadPoolQueue.put(new SocketQueuePacket(PACKET_BUFFER_SIZE, this, packetReadPoolQueue));
		}

		packetWritePoolQueue.clear();
		
		for (int i = 0; i < packetWritePoolQueue.capacity(); i++) {
			packetWritePoolQueue.put(new SocketQueuePacket(PACKET_BUFFER_SIZE, this, packetWritePoolQueue));
		}
		
		sendQueue.clear();
		receiveQueue.clear();
		
		if(existingSocket != null) {

			if(existingSocket.isConnected() && !existingSocket.isClosed()) {
				isConnected = true;
				currentAddress = new InetSocketAddress(existingSocket.getInetAddress().getHostName(), existingSocket.getPort());
			} else {
				return false;
			}
		} else {

			currentAddress = new InetSocketAddress(addressConnect.getHostName(), addressConnect.getPort());
		}
		
		
		Runnable readPacketRunnable = new Runnable() {
			
			@Override
			public void run() {
				
				try(Socket socket = (existingSocket == null) ? new Socket() : existingSocket;) {
					
					socket.setTcpNoDelay(true);
					
					socket.setSendBufferSize(TCP_TX_BUFFER_SIZE);
					socket.setReceiveBufferSize(TCP_RX_BUFFER_SIZE);
					
					//IPTOS_LOWDELAY (0x10)
					socket.setTrafficClass(0x10);
					
					socket.setPerformancePreferences(0, 4, 1);
					
					if(existingSocket == null) {
						
						socket.connect(addressConnect, CONNECT_TIMEOUT);
						
						isConnected = true;
					}
					
					if(socket.getSendBufferSize() != TCP_TX_BUFFER_SIZE) {
						System.err.println("SO_SNDBUF: " + socket.getSendBufferSize() + " Byte. Requested size: " + TCP_TX_BUFFER_SIZE + " Byte");
					}
					if(socket.getReceiveBufferSize() != TCP_RX_BUFFER_SIZE) {
						System.err.println("SO_RCVBUF: " + socket.getReceiveBufferSize() + " Byte. Requested size: " + TCP_RX_BUFFER_SIZE + " Byte");
					}
					
					outputStream = socket.getOutputStream();
					inputStream = socket.getInputStream();
					
					final InputStream in = socket.getInputStream();
					
					// The time intervals spent waiting for empty SocketQueuePackets to get available if the pool runs out.
					// And the time spent waiting between checks for space in the receiveQueue
					while(!Thread.currentThread().isInterrupted()) {
						
						try {
							
							SocketQueuePacket packet = getPacketFromPool(packetReadPoolQueue);
							
							if(packet != null) {

								packet.readFromInputStream(in);
								
								//Try until a spot in the queue is available
								while(!receiveQueue.put(packet)) {
									Thread.sleep(1);
								}
								
							} else {
								Thread.sleep(1);
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
				
				long threadSleepMillis = WRITE_THREAD_SLEEP_MIN;
				
				while(!Thread.currentThread().isInterrupted() && readThreadLocal.isAlive()) {

					try {
						
						if(sendQueue.has()) {
							
							SocketQueuePacket packet = sendQueue.get();
							
							packet.writeToOutputStream(out);
							
							packet.returnToPacketPool();
							
							threadSleepMillis = WRITE_THREAD_SLEEP_MIN;
							
						} else {
							
							Thread.sleep(threadSleepMillis);

							//System.out.println("write-thread slept " + threadSleepMillis + " ms");
							
							threadSleepMillis = Math.min(WRITE_THREAD_SLEEP_MAX, threadSleepMillis + WRITE_THREAD_SLEEP_GAIN);
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
	 * Fetches a SocketQueuePacket from the specified packet-pool.
	 * @return A SocketQueuePacket ready to be used for writing data to it or null if the pool is empty.
	 */
	private SocketQueuePacket getPacketFromPool(AtomicRingBuffer<SocketQueuePacket> pool) {

		SocketQueuePacket packet = pool.get();
		
		if(packet != null) {
			packet.reset();
			packet.unlock();
		}
		
		return packet;
	}

	/**
	 * Fetches a SocketQueuePacket from the write-packet-pool.
	 * @return A SocketQueuePacket ready to be used for writing data to it or null if the pool is empty.
	 */
	public SocketQueuePacket getPacketFromPool() {
		return getPacketFromPool(packetWritePoolQueue);
	}
	
	/**
	 * Returns the remote endpoints address to which this SocketQueue is currently connected.
	 * @return The remote endpoints InetSocketAddress if connected or null.
	 */
	public InetSocketAddress getCurrentAddress() {
		return currentAddress;
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
	 * Unbinds and cleans up the SocketQueue. It can be reused after calling this method.
	 */
	public boolean unbind() {

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
		
		
		packetReadPoolQueue.clear();
		packetWritePoolQueue.clear();
		
		receiveQueue.clear();
		sendQueue.clear();
		
		isBound = isConnected = false;
		
		return true;
	}
}
