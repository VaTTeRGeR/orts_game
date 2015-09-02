package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityObserver;
import com.artemis.systems.EntityProcessingSystem;
import de.vatterger.entitysystem.components.RemoteMaster;
import de.vatterger.entitysystem.components.RemoteMasterInvalidated;
import de.vatterger.entitysystem.interfaces.Modifiable;

public class RemoteMasterRebuildProcessor extends EntityProcessingSystem implements EntityObserver{

	private static ComponentMapper<RemoteMaster> rmm;

	@SuppressWarnings("unchecked")
	public RemoteMasterRebuildProcessor() {
		super(Aspect.getAspectForAll(RemoteMasterInvalidated.class, RemoteMaster.class));
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
				rm.components.add((Modifiable)c);
			} else {
				System.err.println("Error, could not find Component "+rm.classes.get(i)+" on Entity "+e.id+" for RemoteMaster rebuild");
			}
		}
		rm.rebuildComponents = false;
		e.edit().remove(RemoteMasterInvalidated.class);
	}
}
