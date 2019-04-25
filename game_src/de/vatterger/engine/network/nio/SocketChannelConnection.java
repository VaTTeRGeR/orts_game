package de.vatterger.engine.network.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.badlogic.gdx.utils.Queue;

public class SocketChannelConnection {
	
	private final int				connectionId;
	private final InetSocketAddress	address;
	
	private final SelectionKey		key;

	private final Queue<ByteBuffer>	sendQueue;
	
	private int						queueSize = 0;
	
	
	protected SocketChannelConnection(int connectionId, InetSocketAddress address, SelectionKey key) {
		
		this.connectionId = connectionId;
		
		this.address = address;
		
		this.key = key;
		
		sendQueue = new Queue<>(64);
	}
	
	public int getConnectionIdentifier() {
		return connectionId;
	}
	
	public InetSocketAddress getAddress() {
		return address;
	}
	
	protected SelectionKey getKey() {
		return key;
	}
	
	protected Queue<ByteBuffer> getSendQueue() {
		return sendQueue;
	}
	
	protected void addToSendQueue(ByteBuffer packet) {
		sendQueue.addLast(packet);
		queueSize += packet.remaining();
	}
	
	protected boolean writeToChannel(SocketChannel channel) {
		
		while (!sendQueue.isEmpty()) {
		
			ByteBuffer currentBuffer = sendQueue.first();
			
			int bytesWritten = 0;
			
			try {
				
				bytesWritten = channel.write(currentBuffer);
				
			} catch (IOException e) {
				return false;
			}
			
			queueSize -= bytesWritten;
			
			// Stop sending if the ByteBuffer can't be drained completely.
			if(currentBuffer.hasRemaining()) {
				return true;
			} else {
				sendQueue.removeFirst();
			}
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return "SocketChannelConnection-" + getConnectionIdentifier();
	}
}
