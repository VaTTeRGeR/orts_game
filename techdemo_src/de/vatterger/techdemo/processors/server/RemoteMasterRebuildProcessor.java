package de.vatterger.techdemo.processors.server;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.techdemo.components.server.RemoteMaster;
import de.vatterger.techdemo.components.server.RemoteMasterRebuild;
import de.vatterger.techdemo.interfaces.Versionable;

public class RemoteMasterRebuildProcessor extends EntityProcessingSystem {

	private static ComponentMapper<RemoteMaster> rmm;

	public RemoteMasterRebuildProcessor() {
		super(Aspect.all(RemoteMasterRebuild.class, RemoteMaster.class));
	}

	@Override
	protected void initialize() {
		rmm = world.getMapper(RemoteMaster.class);
	}
	
	@Override
	protected void process(Entity e) {
		RemoteMaster rm = rmm.get(e);
		rm.components.clear();
		for (int i = 0; i < rm.classes.size(); i++) {
			Component c = e.getComponent(rm.classes.get(i));
			if(c != null) {
				rm.components.add((Versionable)c);
			} else {
				System.err.println("Error, could not find Component "+rm.classes.get(i)+" on Entity "+e.getId()+" for RemoteMaster rebuild");
			}
		}
		rm.components.trim();
		rm.rebuildComponents = false;
		e.edit().remove(RemoteMasterRebuild.class);
	}
}
