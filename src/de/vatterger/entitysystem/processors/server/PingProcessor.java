package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalEntityProcessingSystem;

import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.components.shared.Ping;

@Wire
public class PingProcessor extends IntervalEntityProcessingSystem {

	private ComponentMapper<KryoConnection> kcm;
	private ComponentMapper<Ping> pm;

	@SuppressWarnings("unchecked")
	public PingProcessor() {
		super(Aspect.all(KryoConnection.class, Ping.class), 2f);
	}

	@Override
	protected void process(Entity e) {
		KryoConnection kc = kcm.get(e);
		if(kc.connection.isConnected()) {
			pm.get(e).ping = kc.connection.getReturnTripTime();
			kc.connection.updateReturnTripTime();
		}
	}
}
