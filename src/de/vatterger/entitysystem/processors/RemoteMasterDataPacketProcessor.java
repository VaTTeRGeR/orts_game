package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.components.ClientConnection;
import de.vatterger.entitysystem.components.DataBucket;
import de.vatterger.entitysystem.components.RemoteMaster;
import de.vatterger.entitysystem.components.ViewFrustum;
import de.vatterger.entitysystem.gridmapservice.GridFlag;
import de.vatterger.entitysystem.gridmapservice.GridMapService;
import de.vatterger.entitysystem.netservice.NetworkService;
import de.vatterger.entitysystem.networkmessages.PacketBundle;
import de.vatterger.entitysystem.networkmessages.RemoteMasterUpdate;

public class RemoteMasterDataPacketProcessor extends EntityProcessingSystem {

	private ComponentMapper<DataBucket> dbm;
	private ComponentMapper<RemoteMaster> rmm;
	private ComponentMapper<ViewFrustum> vfm;
	
	private Bag<Entity> flyweightEntities = new Bag<Entity>(128);

	@SuppressWarnings("unchecked")
	public RemoteMasterDataPacketProcessor() {
		super(Aspect.getAspectForAll(DataBucket.class, ViewFrustum.class));
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
			Entity sendEntity = flyweightEntities.get(i);
			RemoteMaster rm = rmm.get(sendEntity);
			RemoteMasterUpdate rmu = new RemoteMasterUpdate(sendEntity.id, true, rm.components);
			bucket.addData(rmu, 30);
		}
	}
}
