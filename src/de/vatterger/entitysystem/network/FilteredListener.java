package de.vatterger.entitysystem.network;

import java.util.concurrent.LinkedBlockingQueue;

import com.artemis.utils.Bag;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import de.vatterger.entitysystem.network.packets.PacketBundle;

public final class FilteredListener<T> extends Listener {
	
	private final LinkedBlockingQueue<KryoNetMessage<T>> msgQueue = new LinkedBlockingQueue<KryoNetMessage<T>>();
	private final Bag<KryoNetMessage<T>> msgStash = new Bag<KryoNetMessage<T>>(16);
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
	
	public void fillStash() {
		while(!msgQueue.isEmpty()) {
			msgStash.add(msgQueue.poll());
		}
	}
	
	public KryoNetMessage<T> getNext() {
		fillStash();
		if(msgStash.isEmpty()) {
			return null;
		} else {
			return msgStash.remove(0);
		}
	}

	public KryoNetMessage<T> getNext(Connection c) {
		fillStash();
		int msgStashSize = msgStash.size();
		for (int i = 0; i < msgStashSize; i++) {
			if (msgStash.get(i).getConnection().getID() == c.getID()) {
				return msgStash.remove(i);
			}
		}
		return null;
	}
}
