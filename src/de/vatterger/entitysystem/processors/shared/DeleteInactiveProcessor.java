package de.vatterger.entitysystem.processors.shared;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.entitysystem.application.GameConstants;
import de.vatterger.entitysystem.components.shared.Inactive;

public class DeleteInactiveProcessor extends IteratingSystem {

	private ComponentMapper<Inactive>	im;
	private float timeTillDeletion = GameConstants.INACTIVE_DELETION_DELAY;
	
	public DeleteInactiveProcessor(float timeTillDeletion) {
		super(Aspect.all(Inactive.class));
		this.timeTillDeletion = timeTillDeletion;
	}

	public DeleteInactiveProcessor() {
		super(Aspect.all(Inactive.class));
	}
	
	protected void process(int entityId) {
		im.get(entityId).inactiveSince += world.delta;
		if(im.get(entityId).inactiveSince > timeTillDeletion){
			world.getEntity(entityId).deleteFromWorld();
		}
	}
}
