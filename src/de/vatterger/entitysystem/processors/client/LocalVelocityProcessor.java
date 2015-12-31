package de.vatterger.entitysystem.processors.client;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.client.LocalVelocity;
import de.vatterger.entitysystem.components.client.LocalAcceleration;
import de.vatterger.entitysystem.components.shared.Inactive;
@Wire
public class LocalVelocityProcessor extends EntityProcessingSystem {

	ComponentMapper<LocalVelocity>	lvm;
	ComponentMapper<LocalAcceleration>	lam;

	@SuppressWarnings("unchecked")
	public LocalVelocityProcessor() {
		super(Aspect.getAspectForAll(LocalVelocity.class, LocalAcceleration.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		LocalVelocity lvc = lvm.get(e);
		LocalAcceleration lac = lam.get(e);

		if(!lac.acc.isZero()) {
			lvc.vel.add(lac.acc.cpy().scl(e.getWorld().getDelta()));
		}
	}
}
