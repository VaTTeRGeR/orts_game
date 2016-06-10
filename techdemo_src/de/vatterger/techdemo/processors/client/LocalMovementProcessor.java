package de.vatterger.techdemo.processors.client;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.techdemo.components.client.LocalPosition;
import de.vatterger.techdemo.components.client.LocalVelocity;
import de.vatterger.techdemo.components.shared.Inactive;

@Wire
public class LocalMovementProcessor extends EntityProcessingSystem {

	ComponentMapper<LocalPosition>	lpm;
	ComponentMapper<LocalVelocity>	lvm;

	@SuppressWarnings("unchecked")
	public LocalMovementProcessor() {
		super(Aspect.all(LocalPosition.class, LocalVelocity.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		LocalPosition lpc = lpm.get(e);
		LocalVelocity lvc = lvm.get(e);

		if(!lvc.vel.isZero()) {
			lpc.pos.add(lvc.vel.cpy().scl(e.getWorld().getDelta()));
		}
	}
}
