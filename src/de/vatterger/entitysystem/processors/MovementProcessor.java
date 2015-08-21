package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.Velocity;

public class MovementProcessor extends EntityProcessingSystem {

	ComponentMapper<Position>	pm;
	ComponentMapper<Velocity>	vm;

	@SuppressWarnings("unchecked")
	public MovementProcessor() {
		super(Aspect.getAspectForAll(Position.class, Velocity.class));
	}

	@Override
	protected void initialize() {
		pm = world.getMapper(Position.class);
		vm = world.getMapper(Velocity.class);
	}

	protected void process(Entity e) {
		Position pc = pm.get(e);
		Velocity vc = vm.get(e);

		pc.pos.add(vc.vel.cpy().scl(e.getWorld().getDelta()));
		
		//System.out.println("Moved entity "+e.id+" with speed "+vel.vel+" to "+pos.pos);
	}
}
