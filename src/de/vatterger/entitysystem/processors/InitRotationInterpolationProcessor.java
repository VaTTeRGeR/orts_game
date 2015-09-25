package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.ServerRotation;
import de.vatterger.entitysystem.components.RemoteSlave;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.ClientRotation;

public class InitRotationInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerRotation>	rm;

	@SuppressWarnings("unchecked")
	public InitRotationInterpolationProcessor() {
		super(Aspect.getAspectForAll(ServerRotation.class, RemoteSlave.class).exclude(ClientRotation.class, Inactive.class));
	}

	@Override
	protected void initialize() {
		rm = world.getMapper(ServerRotation.class);
	}

	protected void process(Entity e) {
		e.edit().add(new ClientRotation(rm.get(e).rot));
	}
}
