package de.vatterger.techdemo.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.techdemo.components.server.ComponentVersioningRegister;

public class CVRRegisterProcessor extends EntityProcessingSystem {

	private ComponentMapper<ComponentVersioningRegister> cvrm;

	public CVRRegisterProcessor() {
		super(Aspect.all(ComponentVersioningRegister.class));
	}
	
	@Override
	public void inserted(Entity e) {
		ComponentVersioningRegister.cvrs.add(cvrm.get(e));
	}
	
	@Override
	public void removed(Entity e) {
		ComponentVersioningRegister.cvrs.remove(cvrm.get(e));
	}

	@Override
	protected void process(Entity e) {}
}
