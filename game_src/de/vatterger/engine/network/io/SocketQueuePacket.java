package de.vatterger.engine.network.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SocketQueuePacket {
	
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
	
	protected SocketQueuePacket(int size, SocketQueue origin) {
		
		//uid = NEXT_UID.getAndUpdate(updateFunction);
		
		if(size <= 0) throw new IllegalArgumentException("size needs to be > 0.");
		
		if(origin == null) throw new IllegalArgumentException("origin cannot be null.");
		
		data = new byte[size];
		
		buffer = ByteBuffer.wrap(data);
		
		this.origin = origin;
	}
	
	protected void writeToOutputStream(OutputStream out) throws IOException, InterruptedException {
		
		// Only payload size
		int size = buffer.position();

		buffer.flip();
		
		out.write(0xAA);
		out.write(0x55);

		out.write( size >> 8 );
		out.write( size >> 0 );
		
		out.write(data, 0, size);

		out.write(0x55);
		out.write(0xAA);
		
		System.out.println("Wrote packet with " + size + " bytes to OutputStream");
	}
	
	protected void readFromInputStream(InputStream in) throws IOException, InterruptedException {
		
		buffer.clear();
		
		int p0 = in.read();
		int p1 = in.read();
		
		if(p0 != 0xAA || p1 != 0x55) {
			throw new IOException("SocketQueuePacket-Preamble damaged.");
		}
		
		int size = ( in.read() << 8 ) | ( in.read() << 0 );
		
		if(in.readNBytes(data, 0, size) == -1) {
			throw new IOException("End of Stream reached while reading SocketQueuePacket.");
		}
		
		buffer.position(size);
		buffer.limit(buffer.capacity());
		
		p0 = in.read();
		p1 = in.read();
		
		if(p0 != 0x55 || p1 != 0xAA) {
			throw new IOException("SocketQueuePacket-Postamble damaged.");
		}
		
		System.out.println("Read packet with " + size + " bytes from InputStream");
	}
	
	public void returnToPacketPool() {
		origin.returnPacketToPool(this);
	}
	
	public SocketQueue getOrigin() {
		return origin;
	}
	
	@Override
	public String toString() {
		return "SocketQueuePacket: " + data.length + " Byte";
	}
}
