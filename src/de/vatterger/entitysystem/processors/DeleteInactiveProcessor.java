package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.EntityFactory;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.util.GameConstants;

public class DeleteInactiveProcessor extends EntityProcessingSystem {

	ComponentMapper<Inactive>	im;

	@SuppressWarnings("unchecked")
	public DeleteInactiveProcessor() {
		super(Aspect.getAspectForAll(Inactive.class));
	}
	
	@Override
	protected void initialize() {
		im = world.getMapper(Inactive.class);
	}
	
	@Override
	protected void inserted(Entity e) {
		EntityFactory.stripComponentsExcept(e, Inactive.class);
	}
	
	protected void process(Entity e) {
		im.get(e).inactiveSince += world.getDelta();
		if(im.get(e).inactiveSince > GameConstants.INACTIVE_DELETION_DELAY){
			e.deleteFromWorld();
		}
	}
}
