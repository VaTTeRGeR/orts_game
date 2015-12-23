package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.client.ClientRotation;
import de.vatterger.entitysystem.components.client.RemoteSlave;
import de.vatterger.entitysystem.components.server.ServerRotation;
import de.vatterger.entitysystem.components.shared.Inactive;

@Wire
public class InitRotationInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerRotation>	rm;

	@SuppressWarnings("unchecked")
	public InitRotationInterpolationProcessor() {
		super(Aspect.getAspectForAll(ServerRotation.class, RemoteSlave.class).exclude(ClientRotation.class, Inactive.class));
	}

	protected void process(Entity e) {
		e.edit().add(new ClientRotation(rm.get(e).rot));
	}
}
