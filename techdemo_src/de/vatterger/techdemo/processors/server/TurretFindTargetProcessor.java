package de.vatterger.techdemo.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalEntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.gridmap.GridMapBitFlag;
import de.vatterger.engine.handler.gridmap.GridMapHandler;
import de.vatterger.techdemo.components.server.ServerPosition;
import de.vatterger.techdemo.components.server.ServerTurretRotation;
import de.vatterger.techdemo.components.shared.Inactive;
import de.vatterger.techdemo.components.shared.TurretIdle;
import de.vatterger.techdemo.components.shared.TurretTarget;
import de.vatterger.techdemo.components.shared.ViewRange;

@Wire
public class TurretFindTargetProcessor extends IntervalEntityProcessingSystem {

	ComponentMapper<ServerPosition> spm;
	ComponentMapper<TurretTarget> ttm;
	ComponentMapper<ViewRange> vrm;

	private Circle cir = new Circle();
	private Bag<Integer> bag = new Bag<Integer>(128);
	private GridMapBitFlag colFlag = new GridMapBitFlag(GridMapBitFlag.COLLISION | GridMapBitFlag.ACTIVE);

	@SuppressWarnings("unchecked")
	public TurretFindTargetProcessor() {
		super(Aspect.all(ServerPosition.class, ServerTurretRotation.class, ViewRange.class).exclude(Inactive.class, TurretTarget.class, TurretIdle.class), 2f);
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
