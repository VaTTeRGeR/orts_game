/**
* Implementation by VaTTeRGeR
* Reference: "Understanding the Disruptor, a Beginner's Guide to Hardcore Concurrency -Trisha Gee & Mike Barker"
* Link: https://www.youtube.com/watch?v=DCdGlxBbKU4
*/

package de.vatterger.engine.util;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bounded concurrent non-blocking single producer, single consumer ringbuffer for fast inter-thread object passing.
 * */
public class AtomicRingBuffer <T> {

	/** Capacity of the Ringbuffer */
	private final int capacity;

	private long sequenceWriteThreadLocal;

	private final PaddedAtomicLong sequenceWrite;

	private long sequenceReadThreadLocal;

	private final PaddedAtomicLong sequenceRead;

	/** Backing array of the RingBuffer */
	private final T[] data;

	
	/**
	 * Initializes the Ringbuffer with a given capacity.
	 * @param capacity The size of the backing array and therefore capacity of the ringbuffer
	 */
	@SuppressWarnings("unchecked")
	public AtomicRingBuffer(int capacity) {
		
		this.capacity = capacity;
		
		sequenceWrite = new PaddedAtomicLong(-1);
		sequenceWriteThreadLocal = -1;
		
		sequenceRead = new PaddedAtomicLong(0);
		sequenceReadThreadLocal = 0;
		
		data = (T[])new Object[capacity];
	}

	/**
	 * Tries to insert the element into the RingBuffer.
	 * @param element The object to be inserted.
	 * @return true if element could be inserted, false if buffer is full.
	 */
	public boolean put(T element) {
		
		if(canWrite()) {
		
			long index = ++sequenceWriteThreadLocal;
			
			data[(int)(index % capacity)] = element;
			
			sequenceWrite.lazySet(index);
			
			return true;
			
		} else {
			return false;
		}
	}
	
	/**
	 * Returns and removes the oldest element if the RingBuffer is not empty.
	 * @return The oldest element or null if empty.
	 */
	public T get() {
		
		if(has()) {

			T value = data[(int)(sequenceReadThreadLocal++ % capacity)];
			
			sequenceRead.lazySet(sequenceReadThreadLocal);
			
			return value;
		}
		
		return null;
	}
	
	/**
	 * Returns the oldest element if the RingBuffer is not empty. The element is not removed from the RingBuffer.
	 * @return The oldest element or null if empty.
	 */
	public T head() {
		
		if(has()) {
			return data[(int)(sequenceReadThreadLocal % capacity)];
		}
		
		return null;
	}
	
	/**
	 * @return true if the RingBuffer is not empty.
	 */
	public boolean has() {
		return sequenceReadThreadLocal <= sequenceWrite.get();
	}
	
	/**
	 * @return The number of elements that can be retrieved from the RingBuffer at this moment.
	 */
	public long available() {
		return sequenceWrite.get() - sequenceReadThreadLocal + 1;
	}
	
	/**
	 * @return true if the RingBuffer is not full and thus be written to.
	 */
	public boolean canWrite() {
		return ( sequenceWriteThreadLocal - sequenceRead.get() ) < capacity - 1;
	}
	
	/**
	 * Resets the state of the RingBuffer pointers without touching the backing array.
	 */
	public synchronized void clear() {
		
		sequenceWrite.set(-1);
		sequenceRead.set(0);

		sequenceWriteThreadLocal = -1;
		sequenceReadThreadLocal = 0;
	}
	
	/**
	 * Resets the state of the RingBuffer pointers and fills the backing array with null references. Useful to get rid of objects that are being kept alive by residing in this RingBuffer.
	 */
	public synchronized void clearDeep() {
		clear();
		Arrays.fill(data, null);
	}
	
	/**
	 * @return the capacity of the RingBuffer.
	 */
	public int capacity() {
		return capacity;
	}
	
	private class PaddedAtomicLong extends AtomicLong {
		
		private static final long serialVersionUID = 1359216925557380230L;
		
		private final long b8,b16,b24,b32,b40,b48,b56,b64,b72,b80,b88,b96,b104,b112,b120;
		
		private PaddedAtomicLong (int i) {
			super(i);
			
			// Avoid optimizing the padding away!
			b8=b16=b24=b32=b40=b48=b56=b64=b72=b80=b88=b96=b104=b112=b120 = i;
			getSum();
		}
		
		private long getSum() {
			return b8+b16+b24+b32+b40+b48+b56+b64+b72+b80+b88+b96+b104+b112+b120;
		}
	}
}