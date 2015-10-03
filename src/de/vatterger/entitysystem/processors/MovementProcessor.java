package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.ServerPosition;
import de.vatterger.entitysystem.components.Velocity;

public class MovementProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	pm;
	ComponentMapper<Velocity>	vm;

	@SuppressWarnings("unchecked")
	public MovementProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, Velocity.class).exclude(Inactive.class));
	}

	@Override
	protected void initialize() {
		pm = world.getMapper(ServerPosition.class);
		vm = world.getMapper(Velocity.class);
	}

	protected void process(Entity e) {
		ServerPosition pc = pm.get(e);
		Velocity vc = vm.get(e);

		pc.pos.add(vc.vel.cpy().scl(e.getWorld().getDelta()));
		if(!vc.vel.isZero())
			pc.setIsModified();
	}
}
