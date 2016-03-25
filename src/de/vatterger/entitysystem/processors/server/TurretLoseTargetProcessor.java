package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.IntervalEntityProcessingSystem;

import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.server.ServerTurretRotation;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.TurretIdle;
import de.vatterger.entitysystem.components.shared.TurretTarget;
import de.vatterger.entitysystem.components.shared.ViewRange;

public class TurretLoseTargetProcessor extends IntervalEntityProcessingSystem {

	ComponentMapper<ServerPosition> spm;
	ComponentMapper<TurretTarget> ttm;
	ComponentMapper<ViewRange> vrm;
	ComponentMapper<Inactive> iam;

	@SuppressWarnings("unchecked")
	public TurretLoseTargetProcessor() {
		super(Aspect.all(ServerPosition.class, ServerTurretRotation.class, ViewRange.class, TurretTarget.class).exclude(Inactive.class, TurretIdle.class),1f);
	}

	protected void process(Entity e) {
		ServerPosition spc = spm.get(e);
		ViewRange vrc = vrm.get(e);
		TurretTarget ttc = ttm.get(e);
		
		Entity et = world.getEntity(ttc.target);
		
		if(et == null || iam.has(et) || spc.pos.dst(spm.get(world.getEntity(ttc.target)).pos) > vrc.range)
			e.edit().remove(ttc).add(new TurretIdle());
	}
}
