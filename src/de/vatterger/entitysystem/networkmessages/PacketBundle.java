package de.vatterger.entitysystem.networkmessages;

import com.artemis.utils.Bag;

public class PacketBundle {
	
	public Bag<Object> packets = new Bag<Object>();
	private int bytesAvailable;
	private boolean reliable;

	public PacketBundle() {
		reset();
	}
	
	public PacketBundle(int maxBytes, boolean reliable) {
		reset(maxBytes, reliable);
	}
	
	public int add(Object o, int objectBytes) {
		bytesAvailable -= objectBytes;
		packets.add(o);
		return bytesAvailable;
	}
	
	public Bag<Object> getContent() {
		return packets;
	}
	
	public int getBytes() {
		return bytesAvailable;
	}
	
	public boolean hasFreeBytes() {
		return bytesAvailable > 0;
	}
	
	public boolean getReliable(){
		return reliable;
	}
	
	public boolean isEmpty() {
		return packets.isEmpty();
	}
	
	public void reset() {
		reset(1024, false);
	}

	public void reset(int maxBytes, boolean reliable) {
		packets.clear();
		bytesAvailable = maxBytes;
		this.reliable = reliable;
	}
}