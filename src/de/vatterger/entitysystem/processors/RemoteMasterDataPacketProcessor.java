package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
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
	
	private Bag<Integer> flyweightEntities = new Bag<Integer>(128);

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
	protected void process(Entity e) {
		DataBucket bucket = dbm.get(e);
		ViewFrustum vf = vfm.get(e);

		GridMapService.getEntities(new GridFlag(GridFlag.NETWORKED), vf.rect, flyweightEntities);
		
		for (int i = 0; i < flyweightEntities.size(); i++) {
			Entity sendEntity = world.getEntity(flyweightEntities.get(i));
			RemoteMaster rm = rmm.get(sendEntity);
			rm.components.trim();
			RemoteMasterUpdate rmu = new RemoteMasterUpdate(sendEntity.id, true, rm.components);
			bucket.addData(rmu, rm.components.size()*10);
		}
		flyweightEntities.clear();
	}
}
