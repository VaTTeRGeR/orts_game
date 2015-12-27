package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.server.ServerRotation;
import de.vatterger.entitysystem.components.server.ServerTurretRotation;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.TurretTarget;
import de.vatterger.entitysystem.handler.gridmap.GridMapBitFlag;
import de.vatterger.entitysystem.handler.gridmap.GridMapHandler;

@Wire
public class TurretRotateProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition> spm;
	ComponentMapper<ServerRotation> srm;
	ComponentMapper<ServerTurretRotation> strm;
	ComponentMapper<TurretTarget> ttm;

	private Vector3 dif = new Vector3();
	private Circle cir = new Circle();
	private Bag<Integer> bag = new Bag<Integer>(128);
	private GridMapBitFlag colFlag = new GridMapBitFlag(GridMapBitFlag.COLLISION);

	@SuppressWarnings("unchecked")
	public TurretRotateProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, ServerRotation.class, ServerTurretRotation.class,
				TurretTarget.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {

		ServerPosition spc = spm.get(e);
		ServerRotation src = srm.get(e);
		ServerTurretRotation strc = strm.get(e);
		TurretTarget ttc = ttm.get(e);
		bag.clear();
		cir.set(spc.pos.x, spc.pos.y, 50f);

		GridMapHandler.getEntities(colFlag, cir, bag);

		ttc.target = -1;
		Vector3 other = null;
		float dist = Float.MAX_VALUE;
		for (int i = 0; i < bag.size(); i++) {
			other = spm.get(world.getEntity(bag.get(i))).pos;
			if (other.dst(spc.pos) < dist && bag.get(i) != e.getId()) {
				dist = other.dst(spc.pos);
				ttc.target = bag.get(i);
			}
		}

		if (ttc.target > 0) {
			dif.set(spm.get(world.getEntity(ttc.target)).pos).sub(spc.pos);
			strc.rot = (MathUtils.radiansToDegrees * MathUtils.atan2(dif.y, dif.x) - src.rot) % 360f;
			strc.newVersion();
		} else if (strc.rot != 0f) {
			strc.rot = 0f;
			strc.newVersion();
		}
	}
}
