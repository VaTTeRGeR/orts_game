package de.vatterger.entitysystem.network;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import de.vatterger.entitysystem.network.packets.server.PacketBundle;

public final class FilteredListener<T> extends Listener {
	
	private final LinkedBlockingQueue<KryoNetMessage<T>> msgQueue = new LinkedBlockingQueue<KryoNetMessage<T>>();
	private final ArrayList<KryoNetMessage<T>> msgStash = new ArrayList<KryoNetMessage<T>>(16);
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
	
	private void fillStash() {
		msgQueue.drainTo(msgStash);
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
