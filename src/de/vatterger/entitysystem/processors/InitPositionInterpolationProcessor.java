package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.client.ClientPosition;
import de.vatterger.entitysystem.components.client.RemoteSlave;
import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.shared.Inactive;

@Wire
public class InitPositionInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	pm;

	@SuppressWarnings("unchecked")
	public InitPositionInterpolationProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, RemoteSlave.class).exclude(ClientPosition.class, Inactive.class));
	}

	protected void process(Entity e) {
		e.edit().add(new ClientPosition(pm.get(e).pos));
	}
}
