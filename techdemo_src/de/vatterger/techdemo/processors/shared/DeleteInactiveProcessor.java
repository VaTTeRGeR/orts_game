package de.vatterger.techdemo.processors.shared;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.techdemo.application.GameConstants;
import de.vatterger.techdemo.components.shared.Inactive;

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
		if(im.get(entityId).inactiveSince > timeTillDeletion){
			world.getEntity(entityId).deleteFromWorld();
		} else {
			im.get(entityId).inactiveSince += world.delta;
		}
	}
}
