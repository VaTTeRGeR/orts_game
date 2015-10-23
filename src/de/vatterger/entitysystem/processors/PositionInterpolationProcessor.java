package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import de.vatterger.entitysystem.components.ServerPosition;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.ClientPosition;

public class PositionInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	spm;
	ComponentMapper<ClientPosition>	cpm;

	@SuppressWarnings("unchecked")
	public PositionInterpolationProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, ClientPosition.class).exclude(Inactive.class));
	}

	@Override
	protected void initialize() {
		spm = world.getMapper(ServerPosition.class);
		cpm = world.getMapper(ClientPosition.class);
	}

	protected void process(Entity e) {
		ServerPosition spc = spm.get(e);
		ClientPosition cpc = cpm.get(e);
		
		cpc.updateInterpolation(world.getDelta(), spc.pos);
	}
}
