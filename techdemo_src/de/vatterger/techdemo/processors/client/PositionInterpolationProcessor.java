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
public class PositionInterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	spm;
	ComponentMapper<InterpolatedPosition>	cpm;
	ComponentMapper<RemoteSlave>	rsm;

	@SuppressWarnings("unchecked")
	public PositionInterpolationProcessor() {
		super(Aspect.all(ServerPosition.class, InterpolatedPosition.class, RemoteSlave.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		ServerPosition spc = spm.get(e);
		InterpolatedPosition cpc = cpm.get(e);
		
		cpc.updateInterpolation(world.getDelta(), spc.pos, rsm.get(e).lastUpdateDelay == 0f);
	}
}
