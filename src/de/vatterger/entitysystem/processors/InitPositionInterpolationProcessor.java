package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.ServerPosition;
import de.vatterger.entitysystem.components.RemoteSlave;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.ClientPosition;

public class InitPositionInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	pm;

	@SuppressWarnings("unchecked")
	public InitPositionInterpolationProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, RemoteSlave.class).exclude(ClientPosition.class, Inactive.class));
	}

	@Override
	protected void initialize() {
		pm = world.getMapper(ServerPosition.class);
	}

	protected void process(Entity e) {
		e.edit().add(new ClientPosition(pm.get(e).pos));
	}
}
