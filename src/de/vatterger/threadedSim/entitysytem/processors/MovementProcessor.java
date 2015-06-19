package de.vatterger.threadedSim.entitysytem.processors;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.ConcurrentIteratingSystem;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.Velocity;

public class MovementProcessor extends ConcurrentIteratingSystem {

	public MovementProcessor() {
		super(Family.all(Position.class, Velocity.class).get());
	}
	
	@Override
	protected void processEntity(Entity e, float dt) {
		Position p = e.getComponent(Position.class);
		Velocity v = e.getComponent(Velocity.class);
		p.pos.add(v.vel.scl(dt));
		//if(e.getId() == 1L)
		//	System.out.println("Moved Entity "+e.getId()+" to Position "+p.pos.toString()+" "+dt+"dt");
	}

}
