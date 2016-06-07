package de.vatterger.entitysystem.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public abstract class ProgrammableListener<T> extends Listener {
	
	private final Class<T> clazz;
	
	public ProgrammableListener(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public void received(Connection c, Object o) {
		try {
			if (clazz.isInstance(o)) {
				run(c, clazz.cast(o));
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	public abstract void run(Connection c, T received);
}
