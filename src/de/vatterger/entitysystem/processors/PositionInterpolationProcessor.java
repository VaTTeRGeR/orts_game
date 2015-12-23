package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.client.ClientPosition;
import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.shared.Inactive;

@Wire
public class PositionInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	spm;
	ComponentMapper<ClientPosition>	cpm;

	@SuppressWarnings("unchecked")
	public PositionInterpolationProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, ClientPosition.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		ServerPosition spc = spm.get(e);
		ClientPosition cpc = cpm.get(e);
		
		cpc.updateInterpolation(world.getDelta(), spc.pos);
	}
}
