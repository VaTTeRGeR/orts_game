package de.vatterger.threadedSim.entitysytem.processors;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.ConcurrentIteratingSystem;
import com.badlogic.ashley.systems.IteratingSystem;

import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.Velocity;

public class TestComponentPerformanceProcessor extends IteratingSystem {
	private ComponentMapper<Velocity> velMapper = ComponentMapper.getFor(Velocity.class);

	public TestComponentPerformanceProcessor() {
		super(Family.all(Position.class,Velocity.class).get());
	}
	
	@Override
	protected void processEntity(Entity e, final float dt) {
		if(Math.random()>0.9) {
		final Velocity v = velMapper.get(e);
		e.remove(Velocity.class);
		e.add(v);
		}
	}
}
