package de.vatterger.engine.network.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import de.vatterger.engine.util.UnsafeUtil;
import sun.misc.Unsafe;

public class SocketQueuePacket {
	
	private static final Unsafe unsafe = UnsafeUtil.getUnsafe();
	
	private static final long BYTE_ARRAY_OFFSET = unsafe.arrayBaseOffset(byte[].class);

	private static final long INT_ARRAY_OFFSET = unsafe.arrayBaseOffset(int[].class);
	private static final long LONG_ARRAY_OFFSET = unsafe.arrayBaseOffset(long[].class);
	
	private static final long FLOAT_ARRAY_OFFSET = unsafe.arrayBaseOffset(float[].class);
	private static final long DOUBLE_ARRAY_OFFSET = unsafe.arrayBaseOffset(double[].class);
	
	public static final int HEADER_SIZE = 3;
	public static final int POSTAMBLE_SIZE = 1;
	
	private boolean locked = false;
	
	protected int position;
	protected int limit;
	
	protected final byte[] data;
	
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
		
		this.origin = origin;

		reset();
	}
	
	protected void writeToOutputStream(OutputStream out) throws IOException, InterruptedException {
		
		// Only payload size
		int payloadSize = position - HEADER_SIZE;

		data[0] = (byte)0x55;
		
		data[1] = (byte)(payloadSize >> 8);
		data[2] = (byte)(payloadSize >> 0);

		data[position++] = (byte)0xAA;
		
		out.write(data, 0, position);
		
		//System.out.println("Wrote packet with " + payloadSize + " bytes payload to OutputStream");
	}
	
	protected void readFromInputStream(InputStream in) throws IOException, InterruptedException {
		
		if(in.readNBytes(data, 0, HEADER_SIZE) < HEADER_SIZE) {
			throw new IOException("End of Stream reached while reading header of SocketQueuePacket.");
		}

		if(data[0] != (byte)0x55) {
			throw new IOException("SocketQueuePacket-Preamble damaged.");
		}
		
		final int payloadSize =  (data[1] & 0xFF) << 8 | (data[2] & 0xFF) << 0;
		
		if(in.readNBytes(data, HEADER_SIZE, payloadSize + 1) < payloadSize + POSTAMBLE_SIZE) {
			throw new IOException("End of Stream reached while reading payload and postamble of SocketQueuePacket.");
		}
		
		if(data[HEADER_SIZE + payloadSize] != (byte)0xAA) {
			throw new IOException("SocketQueuePacket-Postamble damaged at " + (HEADER_SIZE + payloadSize) + " has value " + data[HEADER_SIZE + payloadSize]);
		}
		
		position = HEADER_SIZE;
		limit = HEADER_SIZE + payloadSize;
		
		//System.out.println("Read packet with " + payloadSize + " bytes from InputStream");
	}
	
	public byte getByte() {
		return data[position++];
	}
	
	public void putByte(byte value) {
		data[position++] = value;
	}
	
	public char getChar() {
		
		final char x = unsafe.getChar(data, BYTE_ARRAY_OFFSET  + position);
		
		position += 2;
		
		return x;
	}
	
	public void putChar(char value) {
		
		unsafe.putChar(data, BYTE_ARRAY_OFFSET + position, value);
		
		position += 2;
	}
	
	public short getShort() {
		
		final short x = unsafe.getShort(data, BYTE_ARRAY_OFFSET  + position);
		
		position += 2;
		
		return x;
	}
	
	public void putShort(short value) {
		
		unsafe.putShort(data, BYTE_ARRAY_OFFSET + position, value);
		
		position += 2;
	}
	
	public int getInt() {
		
		final int x = unsafe.getInt(data, BYTE_ARRAY_OFFSET  + position);
		
		position += 4;
		
		return x;
	}
	
	public void putInt(int value) {
		
		unsafe.putInt(data, BYTE_ARRAY_OFFSET + position, value);
		
		position += 4;
	}
	
	public long getLong() {
		
		final long x = unsafe.getLong(data, BYTE_ARRAY_OFFSET  + position);
		
		position += 8;
		
		return x;
	}
	
	public void putLong(long value) {
		
		unsafe.putLong(data, BYTE_ARRAY_OFFSET + position, value);
		
		position += 8;
	}
	
	public float getFloat() {
		
		final float x = unsafe.getFloat(data, BYTE_ARRAY_OFFSET  + position);
		
		position += 4;
		
		return x;
	}
	
	public void putFloat(float value) {
		
		unsafe.putFloat(data, BYTE_ARRAY_OFFSET + position, value);
		
		position += 4;
	}
	
	public double getDouble() {
		
		final double x = unsafe.getDouble(data, BYTE_ARRAY_OFFSET  + position);
		
		position += 8;
		
		return x;
	}
	
	public void putDouble(double value) {

		unsafe.putDouble(data, BYTE_ARRAY_OFFSET + position, value);
		
		position += 8;
	}
	
	public byte[] getByteArray(int length) {
		
		byte[] dest = new byte[length];
		
		getByteArray(dest, 0, length);
		
		return dest;
	}
	
	public void getByteArray(byte[] dest, int destOffset, int length) {

		System.arraycopy(data, position, dest, destOffset, length);
		
		position += length;
	}
	
	public void putByteArray(byte[] src) {
		putByteArray(src, 0, src.length);
	}
		
	public void putByteArray(byte[] src, int srcOffset, int length) {

		unsafe.copyMemory(src, BYTE_ARRAY_OFFSET + srcOffset, data, BYTE_ARRAY_OFFSET + position, length);

		position += length;
	}
	
	public int[] getIntArray(int length) {
		
		final int[] dest = new int[length];
		
		getIntArray(dest, 0, length);
		
		return dest;
	}
	
	public void getIntArray(int[] dest, int destOffset, int length) {

		length = length << 2;
		destOffset = destOffset << 2;
		
		unsafe.copyMemory(data, BYTE_ARRAY_OFFSET + position, dest, INT_ARRAY_OFFSET + destOffset, length);
		
		position += length;
	}
	
	public void putIntArray(int[] src) {
		putIntArray(src, 0, src.length);
	}
		
	public void putIntArray(int[] src, int srcOffset, int length) {

		srcOffset = srcOffset << 2;
		length = length << 2;
		
		unsafe.copyMemory(src, INT_ARRAY_OFFSET + srcOffset, data, BYTE_ARRAY_OFFSET + position, length);

		position += length;
	}
	
	public long[] getLongArray(int length) {
		
		long[] dest = new long[length];
		
		getLongArray(dest, 0, length);
		
		return dest;
	}
	
	public void getLongArray(long[] dest, int destOffset, int length) {

		length = length << 3;
		destOffset = destOffset << 2;
		
		unsafe.copyMemory(data, BYTE_ARRAY_OFFSET + position, dest, LONG_ARRAY_OFFSET + destOffset, length);
		
		position += length;
	}
	
	public void putLongArray(long[] src) {
		putLongArray(src, 0, src.length);
	}
		
	public void putLongArray(long[] src, int srcOffset, int length) {
		
		length = length << 3;
		
		unsafe.copyMemory(src, LONG_ARRAY_OFFSET + srcOffset, data, BYTE_ARRAY_OFFSET + position, length);

		position += length;
	}
	
	public float[] getFloatArray(int length) {
		
		float[] dest = new float[length];
		
		getFloatArray(dest, 0, length);
		
		return dest;
	}
	
	public void getFloatArray(float[] dest, int destOffset, int length) {

		length = length << 2;
		destOffset = destOffset << 2;
		
		unsafe.copyMemory(data, BYTE_ARRAY_OFFSET + position, dest, FLOAT_ARRAY_OFFSET + destOffset, length);
		
		position += length;
	}
	
	public void putFloatArray(float[] src) {
		putFloatArray(src, 0, src.length);
	}
		
	public void putFloatArray(float[] src, int srcOffset, int length) {
		
		length = length << 2;
		
		unsafe.copyMemory(src, FLOAT_ARRAY_OFFSET + srcOffset, data, BYTE_ARRAY_OFFSET + position, length);
		
		position += length;
	}
	
	public double[] getDoubleArray(int length) {
		
		double[] dest = new double[length];
		
		getDoubleArray(dest, 0, length);
		
		return dest;
	}
	
	public void getDoubleArray(double[] dest, int destOffset, int length) {

		length = length << 3;
		destOffset = destOffset << 2;
		
		unsafe.copyMemory(data, BYTE_ARRAY_OFFSET + position, dest, DOUBLE_ARRAY_OFFSET + destOffset, length);
		
		position += length;
	}
	
	public void putDoubleArray(double[] src) {
		putDoubleArray(src, 0, src.length);
	}
	
	public void putDoubleArray(double[] src, int srcOffset, int length) {
		
		length = length << 3;
		
		unsafe.copyMemory(src, DOUBLE_ARRAY_OFFSET + srcOffset, data, BYTE_ARRAY_OFFSET + position, length);

		position += length;
	}
	
	public byte[] getDataRaw() {
		return data;
	}
	
	public byte[] getPayload() {
		
		int payloadSize = limit - HEADER_SIZE;
		
		byte[] dest = new byte[payloadSize];
		
		System.arraycopy(data, HEADER_SIZE, dest, 0, payloadSize);
		
		return dest;
	}
	
	public void getPayload(byte[] dest) {
		
		int payloadSize = limit - HEADER_SIZE;
		
		System.arraycopy(data, HEADER_SIZE, dest, 0, payloadSize);
	}
	
	public ByteBuffer getDataAsByteBuffer() {
		return ByteBuffer.wrap(data, HEADER_SIZE, limit);
	}
	
	public int position() {
		return position;
	}
	
	public int limit() {
		return limit;
	}
	
	public int remaining() {
		return limit - position;
	}
	
	protected void reset() {

		resetPosition();
		
		resetLimit();
	}
	
	protected void resetPosition() {
		position = HEADER_SIZE;
	}
	
	protected void resetLimit() {
		limit = data.length - POSTAMBLE_SIZE;
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
	
	public void returnToPacketPool() {
		origin.returnPacketToPool(this);
	}
	
	public SocketQueue getOrigin() {
		return origin;
	}
}
