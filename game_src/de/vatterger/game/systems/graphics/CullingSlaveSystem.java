package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.CullingParent;

public class CullingSlaveSystem extends IteratingSystem {

	private ComponentMapper<Culled>				cm;
	private ComponentMapper<CullingParent>		csm;
	
	public CullingSlaveSystem() {
		super(Aspect.all(CullingParent.class));
	}
	
	protected void process(int entityId) {

		CullingParent cs	= csm.get(entityId);
		
		cm.set(entityId, cm.has(cs.parent));
	}
}
