package de.vatterger.engine.network.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Highly performant and asynchronous connection-handler built on TCP Sockets. Handles connecting and disconnecting clients.
 * @see SocketQueue
 */
public class ServerSocketQueue {

	private final ArrayBlockingQueue<SocketQueue> acceptedQueue = new ArrayBlockingQueue<>(2048, false);
	private final ArrayBlockingQueue<SocketQueue> stoppedQueue = new ArrayBlockingQueue<>(2048, false);
	
	private Thread acceptThread = null;
	
	private volatile boolean isBound = false;
	
	public static void main(String[] args) throws InterruptedException {
		
		InetSocketAddress bindAddress = new InetSocketAddress("localhost", 26000);
		
		ServerSocketQueue serverQueue = new ServerSocketQueue();
		
		serverQueue.bind(bindAddress);
		
		Thread.sleep(10000);
		
		serverQueue.stop();
	}
	
	public boolean bind(InetSocketAddress addressBind) {
		
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
					
					serverSocket.setReceiveBufferSize(1024 * 64);
					
					serverSocket.bind(addressBind);
					
					isBound = true;
					
					while(!Thread.interrupted()) {
						
						try {
							
							Socket socket = serverSocket.accept();
							
							SocketQueue queue = new SocketQueue();
							
							if(queue.bind(socket)) {
								runningSocketQueues.add(queue);
								acceptedQueue.offer(queue);
							}
							
						} catch (Exception e) {
							if(e instanceof SocketTimeoutException) {
								// Nothing to see here then
							} else {
								e.printStackTrace();
							}
						}
						
						for (SocketQueue queue : runningSocketQueues) {

							if(!queue.isConnected()) {
								stoppedSocketQueues.add(queue);
								stoppedQueue.offer(queue);
							}
						}
						
						for (SocketQueue queue : stoppedSocketQueues) {
							
							queue.stop();
							
							System.out.println("SocketQueue " + queue + " stopped.");
						}
						
						if(!stoppedSocketQueues.isEmpty()) {
							
							runningSocketQueues.removeAll(stoppedSocketQueues);
							
							stoppedSocketQueues.clear();
						}
					}
					
					for (SocketQueue queue : runningSocketQueues) {
						queue.stop();
						
						stoppedQueue.offer(queue);
						
						System.out.println("SocketQueue " + queue + " stopped.");
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
	public void stop() {

		if(acceptThread != null) {

			acceptThread.interrupt();
			try {
				acceptThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		acceptThread = null;

		acceptedQueue.clear();
		stoppedQueue.clear();
		
		isBound = false;
	}
}
