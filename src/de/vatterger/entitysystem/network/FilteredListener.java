package de.vatterger.entitysystem.network;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.artemis.utils.Bag;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import de.vatterger.entitysystem.network.packets.PacketBundle;

public final class FilteredListener<T> extends Listener {
	
	private final LinkedList<KryoNetMessage<T>> msgQueue = new LinkedList<KryoNetMessage<T>>();
	private final Class<T> clazz;
	
	public FilteredListener(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void received(Connection c, Object o) {
		if (clazz.isInstance(o)) {
			msgQueue.add(new KryoNetMessage<T>((T)o, c));
		} else if(o instanceof PacketBundle) {
			PacketBundle bundle = (PacketBundle)o;
			for (int i = 0; i < bundle.packets.size(); i++) {
				received(c, bundle.packets.get(i));
			}
		}
	}
	
	public KryoNetMessage<T> getNext() {
		return msgQueue.poll();
	}

	public KryoNetMessage<T> getNext(Connection c) {
		int msgQueueSize = msgQueue.size();
		for (int i = 0; i < msgQueueSize; i++) {
			if (msgQueue.get(i).getConnection().getID() == c.getID()) {
				return msgQueue.remove(i);
			}
		}
		return null;
	}
}
