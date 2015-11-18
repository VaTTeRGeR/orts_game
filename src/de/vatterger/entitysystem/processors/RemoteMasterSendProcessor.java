package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.components.server.DataBucket;
import de.vatterger.entitysystem.components.server.RemoteMaster;
import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.shared.GridMapFlag;
import de.vatterger.entitysystem.components.shared.NetPriorityQueue;
import de.vatterger.entitysystem.components.shared.NetSynchedArea;
import de.vatterger.entitysystem.gridmap.GridMapBitFlag;
import de.vatterger.entitysystem.gridmap.GridMapService;
import de.vatterger.entitysystem.network.messages.RemoteMasterUpdate;

public class RemoteMasterSendProcessor extends EntityProcessingSystem {

	private ComponentMapper<DataBucket> dbm;
	private ComponentMapper<RemoteMaster> rmm;
	private ComponentMapper<NetSynchedArea> nsam;
	private ComponentMapper<GridMapFlag> fm;
	private ComponentMapper<ServerPosition> spm;
	
	private Bag<Integer> flyweightEntities = new Bag<Integer>(256);

	@SuppressWarnings("unchecked")
	public RemoteMasterSendProcessor() {
		super(Aspect.getAspectForAll(DataBucket.class, NetSynchedArea.class, NetPriorityQueue.class));
	}

	@Override
	protected void initialize() {
		dbm = world.getMapper(DataBucket.class);
		rmm = world.getMapper(RemoteMaster.class);
		nsam = world.getMapper(NetSynchedArea.class);
		fm = world.getMapper(GridMapFlag.class);
	}
	
	@Override
	protected void process(Entity e) {
		DataBucket bucket = dbm.get(e);
		NetSynchedArea vf = nsam.get(e);
		
		if(bucket.isEmpty()) {
			GridMapService.getEntities(new GridMapBitFlag(GridMapBitFlag.NETWORKED), vf.rect, flyweightEntities);
			for (int i = 0; i < flyweightEntities.size(); i++) {
				Entity sendEntity = world.getEntity(flyweightEntities.get(i));
				if(fm.get(sendEntity).flag.isSuperSetOf(GridMapBitFlag.ACTIVE)) {
					RemoteMaster rm = rmm.get(sendEntity);
					rm.components.trim();
					RemoteMasterUpdate rmu = new RemoteMasterUpdate(sendEntity.id, true, rm.components.getData());
					bucket.addData(rmu, false);
				} else {
					RemoteMasterUpdate rmu = new RemoteMasterUpdate(sendEntity.id, true, new Object[0]); 
					bucket.addData(rmu, false);
				}
			}
			flyweightEntities.clear();
		}
	}
}
