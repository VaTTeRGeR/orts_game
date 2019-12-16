package de.vatterger.engine.network.io;

import de.vatterger.engine.util.AtomicRingBuffer;
import junit.framework.TestCase;

public class RingBufferTest extends TestCase {

	private static final int size = 4;

	public void testPut() {
		
		AtomicRingBuffer<String> buffer = new AtomicRingBuffer<>(size);
		
		assertEquals(true, buffer.put("1"));
		
		assertEquals("1", buffer.head());
		
		assertEquals(1, buffer.available());

		assertEquals(true, buffer.put("2"));
		assertEquals(true, buffer.put("3"));
		assertEquals(true, buffer.put("4"));

		assertEquals(4, buffer.available());

		assertEquals(false, buffer.put("5"));

		assertEquals(4, buffer.available());
	}

	public void testGet() {
		
		AtomicRingBuffer<String> buffer = new AtomicRingBuffer<>(size);
		
		assertNull(buffer.get());
		
		buffer.put("1");
		buffer.put("2");
		buffer.put("3");
		buffer.put("4");
		
		assertEquals("1", buffer.get());
		assertEquals("2", buffer.get());
		assertEquals("3", buffer.get());
		assertEquals("4", buffer.get());

		assertNull(buffer.get());
	}

	public void testHead() {
		
		AtomicRingBuffer<String> buffer = new AtomicRingBuffer<>(size);
		
		assertNull(buffer.head());
		
		assertTrue(buffer.put("1"));
		assertEquals("1", buffer.head());

		assertTrue(buffer.put("2"));
		assertEquals("1", buffer.head());
		
		buffer.get();
		
		assertEquals("2", buffer.head());
		
		buffer.get();

		assertNull(buffer.head());
	}

	public void testHas() {
		AtomicRingBuffer<String> buffer = new AtomicRingBuffer<>(size);
		
		assertFalse(buffer.has());

		assertTrue(buffer.put("1"));

		assertTrue(buffer.has());
	}

	public void testAvailable() {

		AtomicRingBuffer<String> buffer = new AtomicRingBuffer<>(size);
		
		assertEquals(0, buffer.available());

		assertTrue(buffer.put("1"));
		assertEquals(1, buffer.available());
		
		assertTrue(buffer.put("2"));
		assertEquals(2, buffer.available());
		
		buffer.clear();

		assertEquals(0, buffer.available());
	}

	public void testClear() {
		
		AtomicRingBuffer<String> buffer = new AtomicRingBuffer<>(size);
		
		assertTrue(buffer.put("0"));
		assertTrue(buffer.put("1"));
		
		buffer.clear();
		
		assertEquals(0, buffer.available());
		assertEquals(size, buffer.capacity());
		assertNull(buffer.get());
		assertNull(buffer.head());
		assertFalse(buffer.has());
	}

	public void testCapacity() {

		AtomicRingBuffer<String> buffer = new AtomicRingBuffer<>(size);
		
		assertEquals(size, buffer.capacity());
	}
}
