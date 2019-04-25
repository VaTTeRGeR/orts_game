package de.vatterger.engine.network.nio;

import java.nio.ByteBuffer;

public class SocketChannelPacket {
	
	private int connectionId = -1;
	private ByteBuffer buffer = ByteBuffer.allocate(1500);
	
	public ByteBuffer getBuffer() {
		return buffer;
	}
	
	public int getConnectionIdentifier() {
		return connectionId;
	}

	public void setConnectionIdentifier(int id) {
		connectionId = id;
	}
	
	public SocketChannelPacket reset() {
		
		connectionId = -1;
		buffer.clear();
		
		return this;
	}
}
