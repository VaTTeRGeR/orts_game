package de.vatterger.entitysystem.processors.shared;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.Velocity;
@Wire
public class MovementProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	pm;
	ComponentMapper<Velocity>	vm;

	@SuppressWarnings("unchecked")
	public MovementProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, Velocity.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		ServerPosition pc = pm.get(e);
		Velocity vc = vm.get(e);

		if(!vc.vel.isZero()) {
			pc.newVersion();
			pc.pos.add(vc.vel.cpy().scl(e.getWorld().getDelta()));
		}
	}
}
