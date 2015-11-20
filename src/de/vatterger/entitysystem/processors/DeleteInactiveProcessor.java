package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.util.GameUtil;

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
		GameUtil.stripComponentsExcept(e, Inactive.class);
	}
	
	protected void process(Entity e) {
		im.get(e).inactiveSince += world.getDelta();
		if(im.get(e).inactiveSince > GameConstants.INACTIVE_DELETION_DELAY){
			e.deleteFromWorld();
		}
	}
}
