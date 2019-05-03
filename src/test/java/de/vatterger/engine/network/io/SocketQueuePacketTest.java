/**
 * 
 */
package de.vatterger.engine.network.io;

import java.net.Socket;

import junit.framework.TestCase;

/**
 * @author Florian
 *
 */
public class SocketQueuePacketTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	
	SocketQueuePacket packet = null;
	
	protected void setUp() throws Exception {
		SocketQueue queue = new SocketQueue();
		
		queue.bind(new Socket());
		
		packet = queue.getPacketFromPool();
	}

	/**
	 * Test method for {@link de.vatterger.engine.network.io.SocketQueuePacket#isLocked()}.
	 */
	public void testIsLocked() {
		packet.lock();
		assertEquals(true, packet.isLocked());
		
		packet.unlock();
		assertEquals(false, packet.isLocked());
	}

	/**
	 * Test method for {@link de.vatterger.engine.network.io.SocketQueuePacket#lock()}.
	 */
	public void testLock() {
		packet.lock();
		assertEquals(true, packet.isLocked());
	}

	/**
	 * Test method for {@link de.vatterger.engine.network.io.SocketQueuePacket#unlock()}.
	 */
	public void testUnlock() {
		packet.unlock();
		assertEquals(false, packet.isLocked());
	}

}
