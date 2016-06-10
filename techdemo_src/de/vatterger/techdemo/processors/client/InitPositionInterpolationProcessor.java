package de.vatterger.techdemo.processors.client;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.techdemo.components.client.InterpolatedPosition;
import de.vatterger.techdemo.components.client.RemoteSlave;
import de.vatterger.techdemo.components.server.ServerPosition;
import de.vatterger.techdemo.components.shared.Inactive;

@Wire
public class InitPositionInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	pm;

	@SuppressWarnings("unchecked")
	public InitPositionInterpolationProcessor() {
		super(Aspect.all(ServerPosition.class, RemoteSlave.class).exclude(Inactive.class));
	}
	
	@Override
	public void inserted(Entity e) {
		e.edit().add(new InterpolatedPosition(pm.get(e).pos));
	}
	
	@Override
	public void removed(Entity e) {
		e.edit().remove(InterpolatedPosition.class);
	}
	
	@Override
	protected void process(Entity e) {}
}
