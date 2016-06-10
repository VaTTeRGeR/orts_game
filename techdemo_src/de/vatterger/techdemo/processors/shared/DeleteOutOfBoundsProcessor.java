package de.vatterger.techdemo.processors.shared;

import static de.vatterger.techdemo.application.GameConstants.*;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import de.vatterger.techdemo.components.server.ServerPosition;
import de.vatterger.techdemo.components.shared.Inactive;
import de.vatterger.techdemo.factory.shared.EntityModifyFactory;

@Wire
public class DeleteOutOfBoundsProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	pm;
	BoundingBox bounds;

	@SuppressWarnings("unchecked")
	public DeleteOutOfBoundsProcessor() {
		super(Aspect.all(ServerPosition.class).exclude(Inactive.class));
		bounds = new BoundingBox(new Vector3(0, 0, -XY_BOUNDS), new Vector3(XY_BOUNDS, XY_BOUNDS, XY_BOUNDS));
	}
	
	protected void process(Entity e) {
		if(!bounds.contains(pm.get(e).pos)) {
			EntityModifyFactory.deactivateEntityOnGridmap(e);
		}
	}
}
