package de.vatterger.entitysystem.processors.shared;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalEntityProcessingSystem;

import de.vatterger.entitysystem.application.GameConstants;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.factory.shared.EntityModifyFactory;

@Wire
public class DeleteInactiveProcessor extends IntervalEntityProcessingSystem {

	private ComponentMapper<Inactive>	im;
	private float timeTillDeletion = GameConstants.INACTIVE_DELETION_DELAY;

	@SuppressWarnings("unchecked")
	public DeleteInactiveProcessor() {
		super(Aspect.getAspectForAll(Inactive.class), 0.25f);
	}
	
	public DeleteInactiveProcessor(float timeTillDeletion) {
		this();
		this.timeTillDeletion = timeTillDeletion;
	}

	@Override
	protected void inserted(Entity e) {
		EntityModifyFactory.stripComponentsExcept(e, Inactive.class);
	}
	
	protected void process(Entity e) {
		im.get(e).inactiveSince += getIntervalDelta();
		if(im.get(e).inactiveSince > timeTillDeletion){
			e.deleteFromWorld();
		}
	}
}
