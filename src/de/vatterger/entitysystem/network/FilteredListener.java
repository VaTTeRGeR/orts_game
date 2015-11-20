package de.vatterger.entitysystem.network;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public final class FilteredListener<T> extends Listener {
	private final Queue<KryoNetMessage<T>> msg = new ConcurrentLinkedQueue<KryoNetMessage<T>>();
	private final Class<T> clazz;
	
	public FilteredListener(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void received(Connection c, Object o) {
		if (clazz.isInstance(o)) {
			msg.add(new KryoNetMessage<T>((T)o, c));
		} else if(o instanceof PacketBundle) {
			PacketBundle bundle = (PacketBundle)o;
			for (int i = 0; i < bundle.packets.size(); i++) {
				received(c, bundle.packets.get(i));
			}
		}
	}
	
	public KryoNetMessage<T> getNext() {
		return msg.poll();
	}
}
