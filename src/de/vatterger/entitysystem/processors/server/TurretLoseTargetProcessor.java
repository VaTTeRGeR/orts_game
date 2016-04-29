package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalIteratingSystem;
import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.server.ServerTurretRotation;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.TurretIdle;
import de.vatterger.entitysystem.components.shared.TurretTarget;
import de.vatterger.entitysystem.components.shared.ViewRange;

public class TurretLoseTargetProcessor extends IntervalIteratingSystem {

	ComponentMapper<ServerPosition> spm;
	ComponentMapper<TurretTarget> ttm;
	ComponentMapper<ViewRange> vrm;
	ComponentMapper<Inactive> iam;

	@SuppressWarnings("unchecked")
	public TurretLoseTargetProcessor() {
		super(Aspect.all(ServerPosition.class, ServerTurretRotation.class, ViewRange.class, TurretTarget.class).exclude(Inactive.class, TurretIdle.class), 0.5f);
	}

	@Override
	protected void process(int entityId) {
		ServerPosition spc = spm.get(entityId);
		ViewRange vrc = vrm.get(entityId);
		TurretTarget ttc = ttm.get(entityId);

		if (iam.has(ttc.target) || spc.pos.dst(spm.get(ttc.target).pos) > vrc.range) {
			world.edit(entityId).remove(ttc).add(new TurretIdle());
		}
	}
}
