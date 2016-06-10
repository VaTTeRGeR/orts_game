package de.vatterger.techdemo.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.techdemo.components.shared.PreProcessorTask;

@Wire
public class TaskPreProcessor extends EntityProcessingSystem {

	private ComponentMapper<PreProcessorTask> pptm;
	
	@SuppressWarnings("unchecked")
	public TaskPreProcessor() {
		super(Aspect.all(PreProcessorTask.class));
	}
	
	@Override
	protected void process(Entity e) {
		pptm.get(e).runnable.run();
		e.deleteFromWorld();
	}
}
