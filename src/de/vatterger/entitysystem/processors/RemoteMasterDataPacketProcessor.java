package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.IntervalEntityProcessingSystem;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.components.DataBucket;
import de.vatterger.entitysystem.components.RemoteMaster;
import de.vatterger.entitysystem.components.ViewFrustum;
import de.vatterger.entitysystem.gridmapservice.GridFlag;
import de.vatterger.entitysystem.gridmapservice.GridMapService;
import de.vatterger.entitysystem.networkmessages.RemoteMasterUpdate;

public class RemoteMasterDataPacketProcessor extends IntervalEntityProcessingSystem {

	private ComponentMapper<DataBucket> dbm;
	private ComponentMapper<RemoteMaster> rmm;
	private ComponentMapper<ViewFrustum> vfm;
	
	private Bag<Integer> flyweightEntities = new Bag<Integer>(256);
	private Bag<Integer> numEntitiesUpdated = new Bag<Integer>(256);
	
	private int maxUpdatesPerTick = 100;

	@SuppressWarnings("unchecked")
	public RemoteMasterDataPacketProcessor() {
		super(Aspect.getAspectForAll(DataBucket.class, ViewFrustum.class), 0.1f);
	}

	@Override
	protected void initialize() {
		dbm = world.getMapper(DataBucket.class);
		rmm = world.getMapper(RemoteMaster.class);
		vfm = world.getMapper(ViewFrustum.class);
	}
	
	@Override
	protected void inserted(Entity e) {
		numEntitiesUpdated.set(e.id, 0);
	}
	
	@Override
	protected void removed(Entity e) {
		numEntitiesUpdated.set(e.id, null);
	}

	@Override
	protected void process(Entity e) {
		DataBucket bucket = dbm.get(e);
		ViewFrustum vf = vfm.get(e);
		
		int entitiesUpdated = numEntitiesUpdated.get(e.id);
		if(entitiesUpdated > 0) {
			entitiesUpdated -= maxUpdatesPerTick;
		} else {
			GridMapService.getEntities(new GridFlag(GridFlag.NETWORKED), vf.rect, flyweightEntities);
			entitiesUpdated = flyweightEntities.size();
			for (int i = 0; i < flyweightEntities.size(); i++) {
				Entity sendEntity = world.getEntity(flyweightEntities.get(i));
				RemoteMaster rm = rmm.get(sendEntity);
				rm.components.trim();
				RemoteMasterUpdate rmu = new RemoteMasterUpdate(sendEntity.id, true, rm.components.getData());
				bucket.addData(rmu, rm.components.size() * 10);
			}
			flyweightEntities.clear();
		}
		numEntitiesUpdated.set(e.id, entitiesUpdated);
	}
}
