package de.vatterger.techdemo.processors.client;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.techdemo.components.client.InterpolatedTurretRotation;
import de.vatterger.techdemo.components.client.RemoteSlave;
import de.vatterger.techdemo.components.server.ServerTurretRotation;
import de.vatterger.techdemo.components.shared.Inactive;

@Wire
public class InitTurretRotationInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerTurretRotation>	strm;

	@SuppressWarnings("unchecked")
	public InitTurretRotationInterpolationProcessor() {
		super(Aspect.all(ServerTurretRotation.class, RemoteSlave.class).exclude(Inactive.class));
	}

	@Override
	public void inserted(Entity e) {
		e.edit().add(new InterpolatedTurretRotation(strm.get(e).rot));
	}
	
	@Override
	public void removed(Entity e) {
		e.edit().remove(InterpolatedTurretRotation.class);
	}
	
	public void process(Entity e) {}
}
