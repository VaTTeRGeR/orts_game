package de.vatterger.entitysystem.processors;

import static de.vatterger.entitysystem.tools.GameConstants.EXPECTED_ENTITYCOUNT;
import static de.vatterger.entitysystem.tools.GameConstants.XY_BOUNDS;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Circle;

import de.vatterger.entitysystem.components.RemoteMaster;
import de.vatterger.entitysystem.tools.GridPartitionMap;

public class RemoteMasterProcessor extends EntityProcessingSystem {

	private static ComponentMapper<RemoteMaster>	rmm;
	private GridPartitionMap<Circle> map = new GridPartitionMap<Circle>(XY_BOUNDS, EXPECTED_ENTITYCOUNT);

	@SuppressWarnings("unchecked")
	public RemoteMasterProcessor() {
		super(Aspect.getAspectForAll(RemoteMaster.class));
	}

	@Override
	protected void initialize() {
		rmm = world.getMapper(RemoteMaster.class);
	}
	
	@Override
	protected void inserted(Entity e) {
		//Notify clients
	}
	
	@Override
	protected void removed(Entity e) {
		//Notify Clients
	}

	@Override
	protected void process(Entity e) {
		RemoteMaster rm = rmm.get(e);
		if(rm.getNeedsRebuild()) {
			rebuild(e, rm);
		}
		if(rm.getIsChanged()) {
			//D_RemoteMasterUpdate rmmu = new D_RemoteMasterUpdate(e.id, rm.components);
			//Send rmmu to clients!
		}
	}

	public void rebuild(Entity e, RemoteMaster rm) {
		rm.components.clear();
		for (int i = 0; i < rm.classes.size(); i++) {
			Component c = e.getComponent(rm.classes.get(i));
			if(c != null) {
				rm.components.add(c);
			} else {
				System.out.println("Error, could not find Component "+rm.classes.get(i)+" on Entity "+e.id+" for RemoteMaster rebuild");
			}
		}
		rm.rebuildComponents = false;
	}
	
	public static void isChanged(Entity e) {
		if(rmm != null) {
			rmm.get(e).setIsChanged(true);
		}
	}
}
