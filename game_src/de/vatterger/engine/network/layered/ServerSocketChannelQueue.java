package de.vatterger.engine.network.layered;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.badlogic.gdx.math.MathUtils;

public class ServerSocketChannelQueue {
	
	//settings
	
	private		InetSocketAddress					address_bind;
	
	private		volatile	int						updateInterval;

	//queues
	
	protected	BlockingQueue<ByteBuffer>		queue_outgoing;
	protected	BlockingQueue<ByteBuffer>		queue_incoming;
	
	protected	BlockingQueue<ByteBuffer>		queue_bufferPool;
	
	//thread
	
	private		Thread								updateThread;
	
	//nio
	
	private		Selector							selector;
	
	private		ServerSocketChannel					serverSocket;

	
	public ServerSocketChannelQueue(InetSocketAddress address_bind, int updateInterval) {
		this.address_bind		= address_bind;
		this.updateInterval		= updateInterval;
	}

	public boolean bind() {
		
		boolean successful = true;
		
		queue_incoming		= new ArrayBlockingQueue<ByteBuffer>(32*1024);
		queue_outgoing		= new ArrayBlockingQueue<ByteBuffer>(32*1024);
		queue_bufferPool	= new ArrayBlockingQueue<ByteBuffer>(32*1024);

		try {
			
			selector = Selector.open(); // selector is open here
			
			serverSocket = ServerSocketChannel.open();
			
			serverSocket.configureBlocking(false);
			
			serverSocket.bind(address_bind);
			
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
			
			updateThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						
						long time_measured = updateInterval*1000000;
						
						while (!updateThread.isInterrupted()) {
							
							long time = System.nanoTime();
							
							select();
							
							//sleep a bit
							Thread.sleep(MathUtils.clamp(updateInterval*2 - time_measured/1000000, 0, updateInterval));
							
							//measure the time it took to run the lsat iteration
							time_measured = time = System.nanoTime() - time;
						}
					} catch (InterruptedException e) {
						System.err.println("Stopped Thread!");
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
	
	private void select() {

		try {
			selector.select();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Set<SelectionKey> selectedKeys = selector.selectedKeys();

		for (SelectionKey key : selectedKeys) {
			
			
			if (key.isAcceptable() && key.isValid()) {
				
				try {
					acceptChannel();
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
				}

			} else if (key.isWritable() && key.isValid()) {

				SocketChannel client = (SocketChannel) key.channel();
				
				try {
					
					writeChannel(client);

				} catch (IOException e) {
					System.out.println("Client write error.");
				}
			}
		}
		selectedKeys.clear();
	}

	private void acceptChannel() throws IOException, ClosedChannelException {

		SocketChannel client = serverSocket.accept();
		
		client.configureBlocking(false);

		client.register(selector, SelectionKey.OP_READ);
	}

	private void readChannel(SocketChannel client) throws IOException {
		
		ByteBuffer buffer = grabBuffer();
		client.read(buffer);
		System.out.println(new String(buffer.array()).trim());
	}

	private void writeChannel(SocketChannel client) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(64);
		buffer.put("HELLO CLIENT!".getBytes());
		buffer.flip();
		client.write(buffer);
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
		
		queue_bufferPool.clear();

		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	public void write(ByteBuffer packet) {
		queue_outgoing.offer(packet);
	}
	
	public ByteBuffer read() {
		return queue_incoming.poll();
	}

	
	//UPDATE INTERVAL
	
	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}
	
	// Buffer pool
	
	public ByteBuffer grabBuffer() {
		ByteBuffer buffer = queue_bufferPool.poll();

		if(buffer != null) {
			return buffer;
		} else {
			return ByteBuffer.allocate(1400);
		}
	}
	
	public void returnBuffer(ByteBuffer buffer) {
		queue_bufferPool.offer(buffer);
	}
}
