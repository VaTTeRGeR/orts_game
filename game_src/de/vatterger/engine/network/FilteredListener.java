package de.vatterger.engine.network;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public final class FilteredListener<T> extends Listener {
	
	private final LinkedBlockingQueue<KryoNetMessage<T>> msgQueue = new LinkedBlockingQueue<KryoNetMessage<T>>();
	private final LinkedList<KryoNetMessage<T>> msgStash = new LinkedList<KryoNetMessage<T>>();
	private final Class<T> clazz;
	
	public FilteredListener(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void received(Connection c, Object o) {
		if (clazz.isInstance(o)) {
			msgQueue.add(new KryoNetMessage<T>((T)o, c));
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
			return msgStash.poll();
		}
	}

	public KryoNetMessage<T> getNext(int cid) {
		fillStash();
		final int msgStashSize = msgStash.size();
		for (int i = 0; i < msgStashSize; i++) {
			if (msgStash.get(i).getConnection().getID() == cid) {
				return msgStash.remove(i);
			}
		}
		return null;
	}
	
	public void clear() {
		msgStash.clear();
	}
}
