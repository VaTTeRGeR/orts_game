package de.vatterger.entitysystem.processors;

import static de.vatterger.entitysystem.GameConstants.*;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.util.GameUtil;

public class DeleteOutOfBoundsProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	pm;
	BoundingBox bounds;

	@SuppressWarnings("unchecked")
	public DeleteOutOfBoundsProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class).exclude(Inactive.class));
		bounds = new BoundingBox(new Vector3(0, 0, -XY_BOUNDS), new Vector3(XY_BOUNDS, XY_BOUNDS, XY_BOUNDS));
	}
	
	@Override
	protected void initialize() {
		pm = world.getMapper(ServerPosition.class);
	}

	protected void process(Entity e) {
		if(!bounds.contains(pm.get(e).pos)) {
			GameUtil.deactivateEntity(e);
		}
	}
}
