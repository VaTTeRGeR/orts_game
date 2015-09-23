package de.vatterger.entitysystem.processors;

import static de.vatterger.entitysystem.util.GameConstants.*;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import de.vatterger.entitysystem.EntityFactory;
import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.util.GameUtil;

public class DeleteOutOfBoundsProcessor extends EntityProcessingSystem {

	ComponentMapper<Position>	pm;
	BoundingBox bounds;

	public DeleteOutOfBoundsProcessor() {
		super(Aspect.getAspectForAll(Position.class).exclude(Inactive.class));
		bounds = new BoundingBox(new Vector3(0, 0, -XY_BOUNDS), new Vector3(XY_BOUNDS, XY_BOUNDS, XY_BOUNDS));
	}
	
	@Override
	protected void initialize() {
		pm = world.getMapper(Position.class);
	}

	protected void process(Entity e) {
		if(!bounds.contains(pm.get(e).pos)) {
			EntityFactory.deactivateEntity(e);
		}
	}
}
