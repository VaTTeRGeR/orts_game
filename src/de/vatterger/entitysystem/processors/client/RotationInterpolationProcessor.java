package de.vatterger.entitysystem.processors.client;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.client.InterpolatedRotation;
import de.vatterger.entitysystem.components.client.RemoteSlave;
import de.vatterger.entitysystem.components.server.ServerRotation;
import de.vatterger.entitysystem.components.shared.Inactive;

@Wire
public class RotationInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerRotation>	srm;
	ComponentMapper<InterpolatedRotation>	crm;
	ComponentMapper<RemoteSlave>	rsm;

	@SuppressWarnings("unchecked")
	public RotationInterpolationProcessor() {
		super(Aspect.getAspectForAll(ServerRotation.class, InterpolatedRotation.class, RemoteSlave.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		ServerRotation spc = srm.get(e);
		InterpolatedRotation cpc = crm.get(e);
		
		cpc.updateInterpolation(world.getDelta(), spc.rot, rsm.get(e).lastUpdateDelay == 0f);
	}
}
