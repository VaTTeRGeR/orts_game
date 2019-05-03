package de.vatterger.engine.network.io;

import java.util.concurrent.atomic.AtomicLong;

public class RingBuffer <T> {

	private final int SIZE;

	private long sequenceWriteThreadLocal;
	
	private final AtomicLong sequenceWrite;
	
	private final T[] data;

	private long sequenceReadThreadLocal;

	private final AtomicLong sequenceRead;

	
	@SuppressWarnings("unchecked")
	public RingBuffer(int size) {
		
		this.SIZE = size;

		sequenceWrite = new AtomicLong(-1);
		sequenceWriteThreadLocal = -1;

		data = (T[])new Object[size];
		
		sequenceRead = new AtomicLong(0);
		sequenceReadThreadLocal = 0;
	}

	public boolean put(T packet) {
		
		if(canWrite()) {
		
			long index = ++sequenceWriteThreadLocal;
			
			data[(int)(index % SIZE)] = packet;
			
			sequenceWrite.lazySet(index);
			
			return true;
			
		} else {
			return false;
		}
	}
	
	public T get() {
		
		if(sequenceReadThreadLocal <= sequenceWrite.get()) {

			T value = data[(int)(sequenceReadThreadLocal++ % SIZE)];
			
			sequenceRead.lazySet(sequenceReadThreadLocal);

			return value;
		}
		
		return null;
	}
	
	public T head() {
		
		if(sequenceReadThreadLocal <= sequenceWrite.get()) {
			return data[(int)(sequenceReadThreadLocal % SIZE)];
		}
		
		return null;
	}
	
	public boolean has() {
		return sequenceReadThreadLocal <= sequenceWrite.get();
	}

	public long available() {
		return sequenceWrite.get() - sequenceReadThreadLocal + 1;
	}
	
	public boolean canWrite() {
		return sequenceWriteThreadLocal - sequenceRead.get() < SIZE - 1;
	}
	
	public synchronized void clear() {
		sequenceWrite.set(-1);
		sequenceRead.set(0);
		sequenceWriteThreadLocal = -1;
		sequenceReadThreadLocal = 0;
	}

	public int capacity() {
		return SIZE;
	}
}
