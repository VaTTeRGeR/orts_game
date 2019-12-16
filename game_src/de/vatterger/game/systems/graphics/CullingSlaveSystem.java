package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.CullingParent;

public class CullingSlaveSystem extends IteratingSystem {

	private ComponentMapper<Culled>				cm;
	private ComponentMapper<CullingParent>		cpm;
	
	public CullingSlaveSystem() {
		super(Aspect.all(CullingParent.class));
	}
	
	protected void process(int entityId) {

		CullingParent cp = cpm.get(entityId);
		Culled c = cm.getSafe(entityId, null);
		
		if(cp.parent == -1) {
			world.delete(entityId);
		} else {

			final boolean parentCulled = cm.has(cp.parent);
			
			if(c == null && parentCulled)
				world.edit(entityId).add(Culled.flyweight);
			else if(c != null && !parentCulled) {
				world.edit(entityId).remove(Culled.flyweight);
			}
		}
	}
}
