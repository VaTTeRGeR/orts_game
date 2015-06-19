package de.vatterger.threadedSim.entitysytem.processors;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.ConcurrentIteratingSystem;
import com.badlogic.ashley.systems.IteratingSystem;

import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.Velocity;

public class MovementProcessor extends ConcurrentIteratingSystem {
	private ComponentMapper<Position> posMapper = ComponentMapper.getFor(Position.class);
	private ComponentMapper<Velocity> velMapper = ComponentMapper.getFor(Velocity.class);

	public MovementProcessor() {
		super(Family.all(Position.class, Velocity.class).get());
	}
	
	@Override
	protected void processEntity(Entity e, final float dt) {
		final Position p = posMapper.get(e);
		final Velocity v = velMapper.get(e);
		p.pos.add(v.vel.scl(dt));
	}
}
