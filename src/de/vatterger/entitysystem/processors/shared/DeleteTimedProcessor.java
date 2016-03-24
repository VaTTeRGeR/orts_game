package de.vatterger.entitysystem.processors.shared;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.shared.TimedDelete;

@Wire
public class DeleteTimedProcessor extends EntityProcessingSystem {

	private ComponentMapper<TimedDelete>	tdm;

	@SuppressWarnings("unchecked")
	public DeleteTimedProcessor() {
		super(Aspect.all(TimedDelete.class));
	}
	
	protected void process(Entity e) {
		TimedDelete td = tdm.get(e);
		if(td.value <= 0f){
			e.deleteFromWorld();
		} else {
			td.value -= world.getDelta();
		}
	}
}
