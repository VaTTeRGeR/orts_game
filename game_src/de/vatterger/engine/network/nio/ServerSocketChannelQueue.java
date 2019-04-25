package de.vatterger.engine.network.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Queue;

// TODO Finish interfacing with concurrent queues.

/**
 * @author Florian
 */
public class ServerSocketChannelQueue {
	
	// settings
	
	private		InetSocketAddress					address_bind;
	
	/** Timeout for the NIO select in milliseconds*/
	private		volatile long						SELECT_TIMEOUT;
	
	// Connections
	
	private static final	int						MAX_CONNECTIONS = 8;
	
	private Queue<Integer>							freeConnectionIds  = new Queue<>(MAX_CONNECTIONS);
	private Queue<Integer>							boundConnectionIds = new Queue<>(MAX_CONNECTIONS);
	
	private IntMap<SocketChannelConnection>			idToConnectionMap = new IntMap<>(256);
	private ArrayList<SocketChannelConnection>		connections = new ArrayList<>(256);
	
	// queues and buffers
	
	protected	BlockingQueue<SocketChannelPacket>	queue_outgoing;
	protected	BlockingQueue<SocketChannelPacket>	queue_incoming;
	
	protected	BlockingQueue<SocketChannelPacket>	queue_packetPool;
	
	// update-thread
	
	private		Thread								updateThread;
	
	// NIO 
	
	private		Selector							selector;
	
	private		ServerSocketChannel					serverSocket;
	
	public ServerSocketChannelQueue(InetSocketAddress address_bind) {
		this(address_bind, 2);
	}
	
	public ServerSocketChannelQueue(InetSocketAddress address_bind, int updateInterval) {
		
		this.address_bind	= address_bind;
		this.SELECT_TIMEOUT	= updateInterval;
		
		setupConnectionIds();
	}
	
	private void setupConnectionIds() {
		while(freeConnectionIds.size < MAX_CONNECTIONS) {
			freeConnectionIds.addLast(freeConnectionIds.size);
		}
		
		System.out.println("ids: " + freeConnectionIds.size);
	}
	
	public boolean bind() {
		
		boolean successful = true;
		
		queue_incoming		= new ArrayBlockingQueue<SocketChannelPacket>(32*1024);
		queue_outgoing		= new ArrayBlockingQueue<SocketChannelPacket>(32*1024);
		
		queue_packetPool	= new ArrayBlockingQueue<SocketChannelPacket>(32*1024);
		
		try {
			
			selector = Selector.open(); // selector is open here
			
			serverSocket = ServerSocketChannel.open();
			
			serverSocket.configureBlocking(false);
			
			serverSocket.bind(address_bind);
			
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
			
			updateThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					while (!updateThread.isInterrupted()) {
						
						//final long tBeforeSelect = System.nanoTime();
						
						for (SocketChannelConnection connection : connections) {

							SelectionKey key = connection.getKey();
							
							boolean writeEnabled = (key.interestOps() & SelectionKey.OP_WRITE) != 0;
							
							Queue<ByteBuffer> queue = connection.getSendQueue();
							
							if(queue.isEmpty() && writeEnabled) {
								key.interestOpsAnd(~SelectionKey.OP_WRITE);
							} else if(!queue.isEmpty() && !writeEnabled) {
								key.interestOpsOr(SelectionKey.OP_WRITE);
							}
						}
						
						select(SELECT_TIMEOUT);
						
						//final long tAfterSelect = System.nanoTime();
						
						//final long tDeltaSelect = tAfterSelect - tBeforeSelect;
					}
				}
			});
			
			updateThread.start();
			
		} catch (IOException e) {
			successful = false;
			System.err.println("Failed to initialize ServerSocketChannelQueue for " + address_bind.getHostName() + ":" + address_bind.getPort());
			e.printStackTrace();
		}
		
		if(successful) {
			System.out.println("Successfully initialized ServerSocketChannelQueue for " + address_bind.getHostName() + ":" + address_bind.getPort());
		}
		
		return (successful);
	}
	
	public void unbind() {
		
		try {
			updateThread.interrupt();
			updateThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		updateThread = null;
		
		queue_incoming.clear();
		queue_outgoing.clear();
		
		queue_packetPool.clear();
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	private void select(long selectTimeout) {
		
		try {
		
			int keysSelected = selector.select(selectTimeout);

			if(keysSelected == 0) {
				return;
			}
			
		} catch (Exception e) {

			e.printStackTrace();
			
			return;
		}
		
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		
		for (SelectionKey key : selectedKeys) {
			
			SocketChannelConnection connection = (SocketChannelConnection)key.attachment();
			
			if(!key.isValid()) {

				closeConnection(null, connection);

				continue;
			}
			
			if (key.isAcceptable()) {
				
				try {
					acceptChannel();
				} catch (Exception e) {
					System.err.println("Client accept error.");
					e.printStackTrace();
				}
				
			} else if (key.isReadable()) {
					
				SocketChannel channel = (SocketChannel) key.channel();

				if (!readChannel(channel, connection)) {
					closeConnection(channel, connection);
				} else {
					connection.addToSendQueue(ByteBuffer.wrap("HELLO CLIENT!".getBytes()));
				}
				
			} else if (key.isWritable()) {
				
				SocketChannel channel = (SocketChannel) key.channel();
				
				if (!writeChannel(channel, connection)) {
					closeConnection(channel, connection);
				}
			}
		}
		
		selectedKeys.clear();
	}
	
	private void closeConnection(SocketChannel client, SocketChannelConnection connection) {
		
		if(client != null) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(connection != null) {
			
			returnConnectionId(connection.getConnectionIdentifier());
			
			idToConnectionMap.remove(connection.getConnectionIdentifier());

			connections.remove(connection);
			
			System.out.println("Closed connection to " + connection);
		}
		
	}
	
	private void acceptChannel() throws IOException, ClosedChannelException {
		
		SocketChannel channel = serverSocket.accept();
		
		if(freeConnectionIds.isEmpty()) {
			
			System.out.println("Declined client, no more free slots.");
			
			closeConnection(channel, null);
			return;
		}
		
		channel.configureBlocking(false);
		
		SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
		
		SocketChannelConnection connection = new SocketChannelConnection(
				getFreeConnectionId(), (InetSocketAddress)channel.getRemoteAddress(), key);
		
		idToConnectionMap.put(connection.getConnectionIdentifier(), connection);
		
		connections.add(connection);
		
		key.attach(connection);
		
		System.out.println("Accepted " + channel.getRemoteAddress() + " as " + key.attachment() + ".");
	}
	
	private boolean readChannel(SocketChannel channel, SocketChannelConnection connection) {
		
		SocketChannelPacket packet = grabPacketfromPool();
		
		try {

			int bytesRead = channel.read(packet.getBuffer());
			
			if(bytesRead < 0) {
				return false;
			}
			
		} catch (IOException e) {
			return false;
		}
		
		if(connection != null) {
			
			packet.setConnectionIdentifier(connection.getConnectionIdentifier());
			
			queue_incoming.offer(packet);
			
			System.out.println("Received: " + new String(packet.getBuffer().array()).trim() + " from " + connection + ".");
			
		} else {
			System.err.println("Error receiving from: " + connection + ".");
		}
		
		return true;
	}
	
	private boolean writeChannel(SocketChannel channel, SocketChannelConnection connection) {
		return connection.writeToChannel(channel);
	}
	
	public void write(SocketChannelPacket packet) {
		queue_outgoing.offer(packet);
	}
	
	public SocketChannelPacket read() {
		return queue_incoming.poll();
	}
	
	public int getUpdateInterval() {
		return (int)SELECT_TIMEOUT;
	}
	
	public void setUpdateInterval(int updateInterval) {
		this.SELECT_TIMEOUT = updateInterval;
	}
	
	// Buffer pool
	// ######################################################
	public SocketChannelPacket grabPacketfromPool() {
		
		SocketChannelPacket packet = queue_packetPool.poll();

		if(packet != null) {
			return packet.reset();
		} else {
			return new SocketChannelPacket();
		}
	}
	
	public void returnPacketToPool(SocketChannelPacket packet) {
		queue_packetPool.offer(packet);
	}
	
	// ######################################################
	
	private int getFreeConnectionId() {

		int id = freeConnectionIds.removeFirst();
		
		boundConnectionIds.addLast(id);
		
		return id;
	}
	
	private void returnConnectionId(int connectionId) {

		boundConnectionIds.removeValue(connectionId, false);
		
		freeConnectionIds.addLast(connectionId);
	}
}
