package de.vatterger.engine.network.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SocketQueuePacket {
	
	private static final int HEADER_SIZE = 3;
	private static final int POSTAMBLE_SIZE = 1;
	
	private boolean locked = false;
	
	public final ByteBuffer buffer;
	
	public final byte[] data;
	
	private final SocketQueue origin;
	
	/*public final long uid;
	
	private static final AtomicLong NEXT_UID = new AtomicLong(0);
	
	private static final LongUnaryOperator updateFunction = new LongUnaryOperator() {
		
		@Override
		public long applyAsLong(long operand) {
			
			if(operand != Long.MAX_VALUE) {
				return operand + 1;
			} else {
				return 0;
			}
		}
	};
	
	protected static void resetUID() {
		NEXT_UID.set(0);
	}*/
	
	protected SocketQueuePacket(int capacity, SocketQueue origin) {
		
		//uid = NEXT_UID.getAndUpdate(updateFunction);
		
		if(capacity <= 0) throw new IllegalArgumentException("size needs to be > 0.");
		
		if(origin == null) throw new IllegalArgumentException("origin cannot be null.");
		
		data = new byte[capacity + HEADER_SIZE + POSTAMBLE_SIZE];
		
		buffer = ByteBuffer.wrap(data);
		
		buffer.position(HEADER_SIZE);
		
		this.origin = origin;
	}
	
	protected void writeToOutputStream(OutputStream out) throws IOException, InterruptedException {
		
		// next write position - end of user data
		int position = buffer.position();
		// Only payload size
		int size = position - HEADER_SIZE;

		data[0] = (byte)0x55;
		data[1] = (byte)(size >> 8);
		data[2] = (byte)(size >> 0);
		data[position++] = (byte)0xAA;
		
		out.write(data, 0, position);
		
		System.out.println("Wrote packet with " + size + " bytes payload to OutputStream");
	}
	
	protected void readFromInputStream(InputStream in) throws IOException, InterruptedException {
		
		if(in.readNBytes(data, 0, HEADER_SIZE) < HEADER_SIZE) {
			throw new IOException("End of Stream reached while reading header of SocketQueuePacket.");
		}

		if(data[0] != (byte)0x55) {
			throw new IOException("SocketQueuePacket-Preamble damaged.");
		}
		
		final int size = ( data[1] << 8 ) | ( data[2] << 0 );
		
		if(in.readNBytes(data, HEADER_SIZE, size + 1) < size + POSTAMBLE_SIZE) {
			throw new IOException("End of Stream reached while reading payload and postamble of SocketQueuePacket.");
		}
		
		if(data[HEADER_SIZE + size] != (byte)0xAA) {
			throw new IOException("SocketQueuePacket-Postamble damaged at " + (HEADER_SIZE + size) + " has value " + data[HEADER_SIZE + size]);
		}
		
		buffer.limit(size + HEADER_SIZE);
		buffer.position(HEADER_SIZE);
		
		System.out.println("Read packet with " + size + " bytes from InputStream");
	}
	
	public void returnToPacketPool() {
		origin.returnPacketToPool(this);
	}
	
	public SocketQueue getOrigin() {
		return origin;
	}
	
	protected void reset() {
		buffer.clear();
		buffer.position(HEADER_SIZE);
	}
	
	protected boolean isLocked() {
		return locked;
	}
	
	protected void lock() {
		locked = true;
	}
	
	protected void unlock() {
		locked = false;
	}
	
	@Override
	public String toString() {
		return "SocketQueuePacket: " + data.length + " Byte";
	}
}
