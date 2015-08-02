package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.Velocity;

public class MovementProcessor extends EntityProcessingSystem {

	ComponentMapper<Position>	pm;
	ComponentMapper<Velocity>	vm;
	ComponentMapper<CircleCollision>	cm;
	private final static float MARGIN = 0.0001f;

	@SuppressWarnings("unchecked")
	public MovementProcessor() {
		super(Aspect.getAspectForAll(Position.class, Velocity.class));
	}

	@Override
	protected void initialize() {
		pm = world.getMapper(Position.class);
		vm = world.getMapper(Velocity.class);
		cm = world.getMapper(CircleCollision.class);
	}

	protected void process(Entity e) {
		Position pc = pm.get(e);
		Velocity vc = vm.get(e);
		CircleCollision cc = cm.get(e);

		pc.pos.add(vc.vel.cpy().scl(e.getWorld().getDelta()));
		
		//System.out.println("Moved entity "+e.id+" with speed "+vel.vel+" to "+pos.pos);
		
		if(cc != null) {
			cc.circle.setPosition(pc.pos.x, pc.pos.y);
		}
		
		if(vc.vel.isZero(MARGIN)) {
			e.edit().remove(Velocity.class);
		}
		
		RemoteMasterProcessor.isChanged(e);
	}
}
