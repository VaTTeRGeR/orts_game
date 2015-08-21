package de.vatterger.entitysystem.networkmessages;

import com.artemis.utils.Bag;

public class PacketBundle {
	
	public Bag<Object> packets = new Bag<Object>(1);
	int bytes;

	public PacketBundle() {
		this(1000);
	}
	
	public PacketBundle(int maxBytes) {
		reset(maxBytes);
	}
	
	public int add(Object o, int objectBytes) {
		bytes -= objectBytes;
		packets.add(o);
		return bytes;
	}
	
	public Bag<Object> getContent() {
		return packets;
	}
	
	public int getBytes() {
		return bytes;
	}
	
	public boolean hasFreeBytes() {
		return bytes > 0;
	}
	
	
	public void reset() {
		reset(1000);
	}

	public void reset(int maxBytes) {
		packets.clear();
		bytes = maxBytes;
	}
}