package de.vatterger.entitysystem.processors.client;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.client.InterpolatedTurretRotation;
import de.vatterger.entitysystem.components.client.RemoteSlave;
import de.vatterger.entitysystem.components.server.ServerTurretRotation;
import de.vatterger.entitysystem.components.shared.Inactive;

@Wire
public class TurretRotationInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerTurretRotation>	strm;
	ComponentMapper<InterpolatedTurretRotation>	itrm;
	ComponentMapper<RemoteSlave>	rsm;

	@SuppressWarnings("unchecked")
	public TurretRotationInterpolationProcessor() {
		super(Aspect.getAspectForAll(ServerTurretRotation.class, InterpolatedTurretRotation.class, RemoteSlave.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		ServerTurretRotation strc = strm.get(e);
		InterpolatedTurretRotation itrc = itrm.get(e);
		
		itrc.updateInterpolation(world.getDelta(), strc.rot, rsm.get(e).lastUpdateDelay == 0f);
	}
}
