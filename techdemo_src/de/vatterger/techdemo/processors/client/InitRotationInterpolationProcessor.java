package de.vatterger.techdemo.processors.client;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.techdemo.components.client.InterpolatedRotation;
import de.vatterger.techdemo.components.client.RemoteSlave;
import de.vatterger.techdemo.components.server.ServerRotation;
import de.vatterger.techdemo.components.shared.Inactive;

@Wire
public class InitRotationInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerRotation>	rm;

	@SuppressWarnings("unchecked")
	public InitRotationInterpolationProcessor() {
		super(Aspect.all(ServerRotation.class, RemoteSlave.class).exclude(Inactive.class));
	}

	@Override
	public void inserted(Entity e) {
		e.edit().add(new InterpolatedRotation(rm.get(e).rot));
	}
	
	@Override
	public void removed(Entity e) {
		e.edit().remove(InterpolatedRotation.class);
	}
	
	protected void process(Entity e) {}
}
