package de.vatterger.entitysystem.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public abstract class RunMethodListener<T> extends Listener {
	
	private final Class<T> clazz;
	
	public RunMethodListener(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public void received(Connection c, Object o) {
		if (clazz.isInstance(o)) {
			run(c,clazz.cast(o));
		}
	}

	public abstract void run(Connection c, T received);
}
