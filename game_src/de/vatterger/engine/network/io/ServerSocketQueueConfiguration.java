package de.vatterger.engine.network.io;

import de.vatterger.engine.util.AtomicRingBuffer;

public class ServerSocketQueueConfiguration {

	/**This configuration will be used to create the {@link SocketQueue} objects for incoming connections.*/
	public SocketQueueConfiguration NEW_SOCKET_CONFIGURATION = new SocketQueueConfiguration();
	
	/**The maximum time the bind-method is allowed to block for. If binding takes more milliseconds then specified
	 * the process will be canceled.*/
	public int BIND_STARTUP_TIMEOUT = 2000;
	
	/**If true calling {@link ServerSocketQueue}.pollStoppedSocketQueue() will return failed/stopped {@link SocketQueue}s.
	 * If false this method will always return null. If you enable this feature you also need to poll.*/
	public boolean STOPPED_MESSAGE_ENABLE = true;
	
	/**If you do not empty the overflowing accepted/stopped-queue by polling {@link ServerSocketQueue}s pollAcceptedSocketQueue()
	 * and pollStoppedSocketQueue() within this timeframe the {@link ServerSocketQueue} will be stopped.*/
	public int MESSAGE_INSERTION_TIMEOUT = 1000;
	
	/**The amount of accepted {@link SocketQueue}s that can be held in the internal {@link AtomicRingBuffer} before it stops storing them.*/
	public int ACCEPTED_SOCKET_QUEUE_SIZE = 2048;
	
	/**The amount of stopped {@link SocketQueue}s that can be held in the internal {@link AtomicRingBuffer} before it stops storing them.*/
	public int STOPPED_SOCKET_QUEUE_SIZE = 2048;

}
