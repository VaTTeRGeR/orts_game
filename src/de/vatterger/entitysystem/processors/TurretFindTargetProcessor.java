package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.server.ServerTurretRotation;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.TurretIdle;
import de.vatterger.entitysystem.components.shared.TurretTarget;
import de.vatterger.entitysystem.components.shared.ViewRange;
import de.vatterger.entitysystem.handler.gridmap.GridMapBitFlag;
import de.vatterger.entitysystem.handler.gridmap.GridMapHandler;

@Wire
public class TurretFindTargetProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition> spm;
	ComponentMapper<TurretTarget> ttm;
	ComponentMapper<ViewRange> vrm;

	private Circle cir = new Circle();
	private Bag<Integer> bag = new Bag<Integer>(128);
	private GridMapBitFlag colFlag = new GridMapBitFlag(GridMapBitFlag.COLLISION);

	@SuppressWarnings("unchecked")
	public TurretFindTargetProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, ServerTurretRotation.class, ViewRange.class).exclude(Inactive.class, TurretTarget.class, TurretIdle.class));
	}

	protected void process(Entity e) {

		ServerPosition spc = spm.get(e);
		ViewRange vrc = vrm.get(e);

		bag.clear();
		cir.set(spc.pos.x, spc.pos.y, vrc.range);

		GridMapHandler.getEntities(colFlag, cir, bag);

		int target = -1;
		Vector3 other = null;
		float dist = vrc.range;
		
		for (int i = 0; i < bag.size(); i++) {
			other = spm.get(world.getEntity(bag.get(i))).pos;
			if (other.dst(spc.pos) < dist && bag.get(i) != e.getId()) {
				dist = other.dst(spc.pos);
				target = bag.get(i);
			}
		}
		
		if(target == -1)
			e.edit().add(new TurretIdle());
		else
			e.edit().add(new TurretTarget(target));
	}
}
