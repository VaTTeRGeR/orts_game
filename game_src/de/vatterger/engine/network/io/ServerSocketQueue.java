package de.vatterger.engine.network.io;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Highly performant and asynchronous connection-handler built on TCP Sockets. Handles connecting and disconnecting clients.
 * @see SocketQueue
 */
public class ServerSocketQueue {

	private final ArrayBlockingQueue<SocketQueue> acceptedQueue = new ArrayBlockingQueue<>(2048, false);
	private final ArrayBlockingQueue<SocketQueue> stoppedQueue = new ArrayBlockingQueue<>(2048, false);
	
	private volatile Thread acceptThread = null;
	
	private volatile boolean isBound = false;
	
	public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
		
		InetSocketAddress bindAddress = new InetSocketAddress(26000);
		
		ServerSocketQueue serverQueue = new ServerSocketQueue();
		
		serverQueue.bind(bindAddress, 1000);
		
		ArrayList<SocketQueue> clients = new ArrayList<>(256);
		
		byte[] buffer = new byte[8192];
		int[] intBuffer = new int[4];
		
		long msgCounter = 0;
		
		long tPrev = System.currentTimeMillis();
		
		while(serverQueue.isReady()) {
			
			SocketQueue client = null;

			while((client = serverQueue.pollNewSocketQueue()) != null) {
				clients.add(client);
			}
			
			while((client = serverQueue.pollStoppedSocketQueue()) != null) {
				System.out.println("Disconnected: " + client.toString());
			}
			
			for (SocketQueue socketQueue : clients) {

				SocketQueuePacket packet = null;
				
				while((packet = socketQueue.read()) != null) {

					packet.getByteArray(buffer,0,1400);
					
					String message = new String(buffer, "utf-8");
					
					//System.out.println(message);
					
					packet.getIntArray(intBuffer, 0, 4);
					
					//System.out.println(Arrays.toString(intBuffer));

					//System.out.println("Remaining: " + packet.remaining());

					packet.returnToPacketPool();
					
					msgCounter++;
				}
			}
			
			Thread.sleep(1);
			
			if(System.currentTimeMillis() - tPrev > 1000) {

				System.out.println("MSG-Counter: " + msgCounter + " - MByte-Counter: " + (1416*msgCounter/1024/1024));
				
				tPrev = System.currentTimeMillis();
			}
		}
		
		serverQueue.stop();
		
		System.out.println("ServerSocketQueue stopped.");
	}
	
	public boolean bind(InetSocketAddress addressBind, long timeout) {
		
		if(isBound) {
			throw new IllegalStateException("ServerSocketQueue is already bound. Call stop() before binding again.");
		}
		
		if(addressBind == null) {
			throw new IllegalArgumentException("Parameter addressBind cannot be null.");
		}
		
		Runnable acceptRunnable = new Runnable() {
			
			@Override
			public void run() {
				
				final ArrayList<SocketQueue> runningSocketQueues = new ArrayList<>(256);
				final ArrayList<SocketQueue> stoppedSocketQueues = new ArrayList<>(256);
				
				try(ServerSocket serverSocket = new ServerSocket();) {
					
					serverSocket.setSoTimeout(10);
					
					serverSocket.setReceiveBufferSize(64 * 1024);
					
					serverSocket.bind(addressBind);
					
					isBound = true;
					
					while(!Thread.currentThread().isInterrupted()) {
						
						try {
							
							Socket socket = serverSocket.accept();
							
							SocketQueue queue = new SocketQueue();
							
							if(queue.bind(socket)) {
								runningSocketQueues.add(queue);
								acceptedQueue.offer(queue);
							}
							
						} catch (SocketTimeoutException e) {
							//
						}
						
						for (SocketQueue queue : runningSocketQueues) {
							
							if(!queue.isReady()) {
								
								stoppedSocketQueues.add(queue);

								stoppedQueue.offer(queue);
							}
						}
						
						for (SocketQueue queue : stoppedSocketQueues) {
							
							while(!queue.stop()) {

								System.out.println("Trying to stop " + queue);
								
								Thread.currentThread().interrupt();
								
								Thread.yield();
							}
							
							System.out.println("Stopped " + queue);
						}
						
						if(!stoppedSocketQueues.isEmpty()) {
							
							runningSocketQueues.removeAll(stoppedSocketQueues);
							
							stoppedSocketQueues.clear();
						}
					}
					
					System.out.println("Exited ServerSocketQueue loop.");
					
					for (SocketQueue queue : runningSocketQueues) {
						
						while(!queue.stop()) {
							Thread.yield();
						}
						
						stoppedQueue.offer(queue);

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
		
		long tStart = System.currentTimeMillis();
		
		// timeout: -1 => Return immediately
		if(timeout == -1) {
			return true;
		}
		
		while (!isReady()) {
			// timeout: 0 => wait indefinitely
			if(timeout > 0 && System.currentTimeMillis() - tStart >= timeout) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * SocketQueues that have been successfully created can be retrieved via this method.
	 * @return A newly bound SocketQueue if one is available or otherwise null.
	 */
	public SocketQueue pollNewSocketQueue() {
		return acceptedQueue.poll();
	}
	
	/**
	 * SocketQueues that have stopped because of a disconnect or error can be retrieved via this method.
	 * @return A stopped SocketQueue if one is available or otherwise null.
	 */
	public SocketQueue pollStoppedSocketQueue() {
		return stoppedQueue.poll();
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
	public boolean stop() {

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
