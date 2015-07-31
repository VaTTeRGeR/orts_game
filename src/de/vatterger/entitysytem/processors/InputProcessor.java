package de.vatterger.entitysytem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.ViewFrustum;
import de.vatterger.entitysystem.components.Velocity;

public class InputProcessor extends EntityProcessingSystem {

	ComponentMapper<Velocity> vm;
	
	@SuppressWarnings("unchecked")
	public InputProcessor() {
		super(Aspect.getAspectForAll(ViewFrustum.class));
	}

	@Override
	protected void initialize() {
		vm = world.getMapper(Velocity.class);
	}

	protected void process(Entity e) {
		
	}
}
