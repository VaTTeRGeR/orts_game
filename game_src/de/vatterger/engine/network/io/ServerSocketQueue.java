package de.vatterger.engine.network.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import de.vatterger.engine.util.AtomicRingBuffer;

/**
 * Highly performant and asynchronous connection-handler built on TCP Sockets. Handles connecting and disconnecting clients.
 * @see SocketQueue
 */
public class ServerSocketQueue {

	private final SocketQueueConfiguration NEW_SOCKET_CONFIGURATION;

	private final int BIND_STARTUP_TIMEOUT;

	public final boolean STOPPED_MESSAGE_ENABLE;

	public final int MESSAGE_INSERTION_TIMEOUT;
	
	private final int TCP_RX_SUGGESTED_BUFFER_SIZE;
	
	private final int ACCEPTED_SOCKET_QUEUE_SIZE;
	private final int STOPPED_SOCKET_QUEUE_SIZE;
	
	
	private final AtomicRingBuffer<SocketQueue> acceptedQueue;
	private final AtomicRingBuffer<SocketQueue> stoppedQueue;
	
	private volatile Thread acceptThread = null;
	
	private volatile boolean isBound = false;


	public ServerSocketQueue() {
		this(new ServerSocketQueueConfiguration());
	}
	
	public ServerSocketQueue(ServerSocketQueueConfiguration configuration) {
		
		NEW_SOCKET_CONFIGURATION = configuration.NEW_SOCKET_CONFIGURATION;
		
		BIND_STARTUP_TIMEOUT = configuration.BIND_STARTUP_TIMEOUT;

		STOPPED_MESSAGE_ENABLE = configuration.STOPPED_MESSAGE_ENABLE;
		
		MESSAGE_INSERTION_TIMEOUT = configuration.MESSAGE_INSERTION_TIMEOUT;
		
		TCP_RX_SUGGESTED_BUFFER_SIZE = NEW_SOCKET_CONFIGURATION.TCP_RX_BUFFER_SIZE;
		
		ACCEPTED_SOCKET_QUEUE_SIZE = configuration.ACCEPTED_SOCKET_QUEUE_SIZE;
		STOPPED_SOCKET_QUEUE_SIZE = configuration.STOPPED_SOCKET_QUEUE_SIZE;
		
		acceptedQueue = new AtomicRingBuffer<>(ACCEPTED_SOCKET_QUEUE_SIZE);
		stoppedQueue = new AtomicRingBuffer<>(STOPPED_SOCKET_QUEUE_SIZE);
	}
	
	public boolean bind(InetSocketAddress addressBind) {
		
		long tStartBind = System.currentTimeMillis();

		if(isBound) {
			throw new IllegalStateException("ServerSocketQueue is already bound. Call stop() before binding again.");
		}
		
		if(addressBind == null) {
			throw new IllegalArgumentException("Parameter addressBind cannot be null.");
		}
		
		Runnable acceptRunnable = new Runnable() {
			
			@Override
			public void run() {
				
				//Keeps track of currently active connections
				final ArrayList<SocketQueue> runningSocketQueues = new ArrayList<>(512);
				//Keeps track of recently killed connections to allow post-processing of dead connections
				final ArrayList<SocketQueue> stoppedSocketQueues = new ArrayList<>(512);

				try(ServerSocket serverSocket = new ServerSocket();) {
					
					serverSocket.setSoTimeout(10);
					
					serverSocket.setReceiveBufferSize(TCP_RX_SUGGESTED_BUFFER_SIZE);
					
					serverSocket.setPerformancePreferences(0, 4, 1);
					
					serverSocket.bind(addressBind);
					
					isBound = true;
					
					while(!Thread.currentThread().isInterrupted()) {
						
						final long tCycleStart = System.currentTimeMillis();
						
						try {
							
							Socket socket = serverSocket.accept();
							
							SocketQueue queue = new SocketQueue(NEW_SOCKET_CONFIGURATION);
							
							if(queue.bind(socket)) {
								
								runningSocketQueues.add(queue);
								
								while(!acceptedQueue.put(queue)) {
									
									if(MESSAGE_INSERTION_TIMEOUT > 0 && System.currentTimeMillis() - tCycleStart >= MESSAGE_INSERTION_TIMEOUT) {
										Thread.currentThread().interrupt();
										break;
									}
									
									try {
										Thread.sleep(1);
									} catch (InterruptedException e) {
										Thread.currentThread().interrupt();
										break;
									}
								}
							}
							
						} catch (SocketTimeoutException   e) {
						} catch (IOException | SecurityException e) {
							e.printStackTrace();
						}
						
						//Remove Failed Connections from the running list
						for (SocketQueue queue : runningSocketQueues) {
							
							if(!queue.isReady()) {
								
								stoppedSocketQueues.add(queue);
								
								while(!stoppedQueue.put(queue)) {
									
									if(MESSAGE_INSERTION_TIMEOUT > 0 && System.currentTimeMillis() - tCycleStart >= MESSAGE_INSERTION_TIMEOUT) {
										Thread.currentThread().interrupt();
										break;
									}
									
									try {
										Thread.sleep(1);
									} catch (InterruptedException e) {
										Thread.currentThread().interrupt();
									}
								}
							}
						}
						
						//Call unbind on all stopped SocketQueues to clean up
						for (SocketQueue queue : stoppedSocketQueues) {
							
							while(!queue.unbind()) {

								Thread.currentThread().interrupt();
								
								Thread.yield();
							}
						}
						
						//Clear the stoppedSocketQueues list
						if(!stoppedSocketQueues.isEmpty()) {
							
							runningSocketQueues.removeAll(stoppedSocketQueues);
							
							stoppedSocketQueues.clear();
						}
					}
					
					for (SocketQueue queue : runningSocketQueues) {
						
						while(!queue.unbind()) {
							Thread.yield();
						}
						
						boolean success = stoppedQueue.put(queue);

						System.out.println("Stopped " + queue);
					}
					
					runningSocketQueues.clear();
					stoppedSocketQueues.clear();
					
					isBound = false;
					
				} catch (IOException e) {
					isBound = false;
					e.printStackTrace();
				}
			}
		};
		
		acceptThread = new Thread(acceptRunnable, "ServerSocket-ACCEPT-Thread");

		acceptThread.setDaemon(true);

		acceptThread.start();
		
		while (!isReady()) {
			
			if(System.currentTimeMillis() - tStartBind > BIND_STARTUP_TIMEOUT) {

				unbind();
				
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * SocketQueues that have been successfully created can be retrieved via this method.
	 * @return A newly bound SocketQueue if one is available or otherwise null.
	 */
	public SocketQueue pollAcceptedSocketQueue() {
		return acceptedQueue.get();
	}
	
	/**
	 * SocketQueues that have stopped because of a disconnect or error can be retrieved via this method.
	 * @return A stopped SocketQueue if one is available or otherwise null.
	 */
	public SocketQueue pollStoppedSocketQueue() {
		return stoppedQueue.get();
	}
	
	/**
	 * The ServerSocketQueue is ready to use once it is bound and connected.
	 * @return True if ready to use, otherwise false.
	 */
	public boolean isReady() {
		return isBound;
	}
	
	/**
	 * Unbinds and cleans up the ServerSocketQueue.
	 */
	public boolean unbind() {

		if(acceptThread != null && acceptThread.isAlive()) {
			
			acceptThread.interrupt();

			try {
				
				acceptThread.join();
				
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
		}

		acceptedQueue.clear();
		stoppedQueue.clear();
		
		acceptThread = null;
		
		isBound = false;
		
		return true;
	}
}
