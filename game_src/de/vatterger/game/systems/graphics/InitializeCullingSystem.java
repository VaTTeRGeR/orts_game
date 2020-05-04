package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;

import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.CullingParent;
import de.vatterger.game.components.gameobject.NotCulled;

public class InitializeCullingSystem extends BaseEntitySystem {
	
	public InitializeCullingSystem() {
		super(Aspect.all(AbsolutePosition.class, CullDistance.class).exclude(Culled.class, NotCulled.class, CullingParent.class));
	}
	
	@Override
	protected void inserted (int entityId) {
		world.edit(entityId).add(Culled.flyweight);
	}
	
	@Override
	protected void processSystem () {
	}
}
