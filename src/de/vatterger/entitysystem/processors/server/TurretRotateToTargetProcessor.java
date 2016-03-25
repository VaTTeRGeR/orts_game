package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.server.ServerRotation;
import de.vatterger.entitysystem.components.server.ServerTurretRotation;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.TurretIdle;
import de.vatterger.entitysystem.components.shared.TurretTarget;

@Wire
public class TurretRotateToTargetProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition> spm;
	ComponentMapper<ServerRotation> srm;
	ComponentMapper<ServerTurretRotation> strm;
	ComponentMapper<TurretTarget> ttm;

	private Vector3 dif = new Vector3();
	private float dir = 0f;

	@SuppressWarnings("unchecked")
	public TurretRotateToTargetProcessor() {
		super(Aspect.all(ServerPosition.class, ServerRotation.class, ServerTurretRotation.class).one(TurretTarget.class, TurretIdle.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {

		ServerPosition spc = spm.get(e);
		ServerRotation src = srm.get(e);
		ServerTurretRotation strc = strm.get(e);

		dir = strc.rot;
		if (ttm.has(e)) {
			TurretTarget ttc = ttm.get(e);
			dif.set(spm.get(world.getEntity(ttc.target)).pos).sub(spc.pos);
			strc.rot = (MathUtils.radiansToDegrees * MathUtils.atan2(dif.y, dif.x) - src.rot) % 360f;
		} else {
			strc.rot = 0f;
			e.edit().remove(TurretIdle.class);
		}
		if(!MathUtils.isEqual(dir, strc.rot, 360f/64f)) {
			strc.newVersion();
		}
	}
}
