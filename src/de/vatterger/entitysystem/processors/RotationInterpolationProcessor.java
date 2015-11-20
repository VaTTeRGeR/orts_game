package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.client.ClientRotation;
import de.vatterger.entitysystem.components.server.ServerRotation;

public class RotationInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerRotation>	srm;
	ComponentMapper<ClientRotation>	crm;

	@SuppressWarnings("unchecked")
	public RotationInterpolationProcessor() {
		super(Aspect.getAspectForAll(ServerRotation.class, ClientRotation.class).exclude(Inactive.class));
	}

	@Override
	protected void initialize() {
		srm = world.getMapper(ServerRotation.class);
		crm = world.getMapper(ClientRotation.class);
	}

	protected void process(Entity e) {
		ServerRotation spc = srm.get(e);
		ClientRotation cpc = crm.get(e);
		
		cpc.updateInterpolation(world.getDelta(), spc.rot);
	}
}
