package de.vatterger.engine.network.io;

import junit.framework.TestCase;

public class RingBufferTest extends TestCase {

	private static final int size = 4;

	public void testPut() {
		
		RingBuffer<String> buffer = new RingBuffer<>(size);
		
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
		
		RingBuffer<String> buffer = new RingBuffer<>(size);
		
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
		
		RingBuffer<String> buffer = new RingBuffer<>(size);
		
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
		RingBuffer<String> buffer = new RingBuffer<>(size);
		
		assertFalse(buffer.has());

		assertTrue(buffer.put("1"));

		assertTrue(buffer.has());
	}

	public void testAvailable() {

		RingBuffer<String> buffer = new RingBuffer<>(size);
		
		assertEquals(0, buffer.available());

		assertTrue(buffer.put("1"));
		assertEquals(1, buffer.available());
		
		assertTrue(buffer.put("2"));
		assertEquals(2, buffer.available());
		
		buffer.clear();

		assertEquals(0, buffer.available());
	}

	public void testClear() {
		
		RingBuffer<String> buffer = new RingBuffer<>(size);
		
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

		RingBuffer<String> buffer = new RingBuffer<>(size);
		
		assertEquals(size, buffer.capacity());
	}
}
