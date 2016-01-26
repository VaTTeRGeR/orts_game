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
public class InitTurretRotationInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerTurretRotation>	strm;

	@SuppressWarnings("unchecked")
	public InitTurretRotationInterpolationProcessor() {
		super(Aspect.getAspectForAll(ServerTurretRotation.class, RemoteSlave.class).exclude(Inactive.class));
	}

	@Override
	protected void inserted(Entity e) {
		e.edit().add(new InterpolatedTurretRotation(strm.get(e).rot));
	}
	
	@Override
	protected void removed(Entity e) {
		e.edit().remove(InterpolatedTurretRotation.class);
	}
	
	protected void process(Entity e) {}
}
