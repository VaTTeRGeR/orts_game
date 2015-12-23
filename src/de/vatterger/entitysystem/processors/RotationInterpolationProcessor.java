package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.client.ClientRotation;
import de.vatterger.entitysystem.components.server.ServerRotation;
import de.vatterger.entitysystem.components.shared.Inactive;

@Wire
public class RotationInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerRotation>	srm;
	ComponentMapper<ClientRotation>	crm;

	@SuppressWarnings("unchecked")
	public RotationInterpolationProcessor() {
		super(Aspect.getAspectForAll(ServerRotation.class, ClientRotation.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		ServerRotation spc = srm.get(e);
		ClientRotation cpc = crm.get(e);
		
		cpc.updateInterpolation(world.getDelta(), spc.rot);
	}
}
