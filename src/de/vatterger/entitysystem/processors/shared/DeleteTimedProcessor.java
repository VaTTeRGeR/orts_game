package de.vatterger.entitysystem.processors.shared;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.entitysystem.components.shared.TimedDelete;

public class DeleteTimedProcessor extends IteratingSystem {

	private ComponentMapper<TimedDelete>	tdm;

	public DeleteTimedProcessor() {
		super(Aspect.all(TimedDelete.class));
	}
	
	protected void process(int entityId) {
		TimedDelete td = tdm.get(entityId);
		td.value -= world.getDelta();
		if(td.value <= 0f) {
			world.delete(entityId);
		}
	}
}
