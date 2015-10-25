package de.vatterger.entitysystem.netservice;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import de.vatterger.entitysystem.networkmessages.PacketBundle;

public final class QFUPListener<T> extends Listener {
	private final Queue<MessageRemote<T>> msg = new ConcurrentLinkedQueue<MessageRemote<T>>();
	private final Class<T> clazz;
	
	public QFUPListener(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void received(Connection c, Object o) {
		if (clazz.isInstance(o)) {
			msg.add(new MessageRemote<T>((T)o, c));
		} else if(o instanceof PacketBundle) {
			PacketBundle bundle = (PacketBundle)o;
			for (int i = 0; i < bundle.packets.size(); i++) {
				received(c, bundle.packets.get(i));
			}
		}
	}
	
	public MessageRemote<T> getNext() {
		return msg.poll();
	}
}
