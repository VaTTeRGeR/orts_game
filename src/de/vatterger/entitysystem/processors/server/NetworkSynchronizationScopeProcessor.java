package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

public class NetworkSynchronizationScopeProcessor extends EntityProcessingSystem {


	public NetworkSynchronizationScopeProcessor() {
		super(Aspect.all());
	}

	@Override
	protected void process(Entity e) {
		
	}
}
