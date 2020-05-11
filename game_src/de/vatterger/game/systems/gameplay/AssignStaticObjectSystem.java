package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;

import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.MoveCurve;
import de.vatterger.game.components.gameobject.StaticObject;
import de.vatterger.game.components.gameobject.Velocity;

public class AssignStaticObjectSystem extends BaseEntitySystem {
	
	public AssignStaticObjectSystem() {
		super(Aspect.all(AbsolutePosition.class).exclude(MoveCurve.class, Velocity.class));
	}
	
	@Override
	protected void inserted (int entityId) {
		world.edit(entityId).add(StaticObject.SHARED_INSTANCE);
	}
	
	@Override
	protected void removed (int entityId) {
		world.edit(entityId).remove(StaticObject.SHARED_INSTANCE);
	}

	@Override
	protected void processSystem () {}
}
