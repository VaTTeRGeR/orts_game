package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.ServerPosition;
import de.vatterger.entitysystem.components.RemoteSlave;
import de.vatterger.entitysystem.interfaces.Interpolatable;
import de.vatterger.entitysystem.util.GameUtil;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.ClientPosition;

public class InterpolationProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	spm;
	ComponentMapper<ClientPosition>	cpm;

	@SuppressWarnings("unchecked")
	public InterpolationProcessor() {
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
