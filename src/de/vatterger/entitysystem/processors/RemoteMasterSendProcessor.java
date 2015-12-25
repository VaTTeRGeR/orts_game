package de.vatterger.entitysystem.processors;

import java.util.LinkedList;
import java.util.Queue;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.components.server.ComponentVersioningRegister;
import de.vatterger.entitysystem.components.server.DataBucket;
import de.vatterger.entitysystem.components.server.EntityAckBucket;
import de.vatterger.entitysystem.components.server.RemoteMaster;
import de.vatterger.entitysystem.components.shared.GridMapFlag;
import de.vatterger.entitysystem.components.shared.NetPriorityQueue;
import de.vatterger.entitysystem.components.shared.NetSynchedArea;
import de.vatterger.entitysystem.handler.gridmap.GridMapBitFlag;
import de.vatterger.entitysystem.handler.gridmap.GridMapHandler;
import de.vatterger.entitysystem.network.packets.RemoteMasterUpdate;

@Wire
public class RemoteMasterSendProcessor extends EntityProcessingSystem {

	private ComponentMapper<DataBucket> dbm;
	private ComponentMapper<RemoteMaster> rmm;
	private ComponentMapper<NetSynchedArea> nsam;
	private ComponentMapper<GridMapFlag> fm;
	private ComponentMapper<EntityAckBucket> eabm;
	private ComponentMapper<ComponentVersioningRegister> cvrm;
	
	private Bag<Integer> flyweightEntities = new Bag<Integer>(256);

	@SuppressWarnings("unchecked")
	public RemoteMasterSendProcessor() {
		super(Aspect.getAspectForAll(DataBucket.class, NetSynchedArea.class, NetPriorityQueue.class, EntityAckBucket.class, ComponentVersioningRegister.class));
	}

	@Override
	protected void process(Entity e) {
		DataBucket bucket = dbm.get(e);
		NetSynchedArea vf = nsam.get(e);
		EntityAckBucket eab = eabm.get(e);
		ComponentVersioningRegister cvr = cvrm.get(e);
		
		Queue<Object> componentQueue = new LinkedList<Object>();

		if(bucket.isEmpty()) {
			GridMapHandler.getEntities(new GridMapBitFlag(GridMapBitFlag.NETWORKED), vf.rect, flyweightEntities);
			for (int i = 0; i < flyweightEntities.size(); i++) {
				Entity sendEntity = world.getEntity(flyweightEntities.get(i));
				if(fm.get(sendEntity).flag.isSuperSetOf(GridMapBitFlag.ACTIVE)) {
					
					boolean alreadyReceived = false;
					int eid = sendEntity.id;
					for (int j = 0; j < eab.ids.size(); j++) {
						if(eab.ids.get(j).equals(eid))
							alreadyReceived = true;
					}
					
					if(alreadyReceived){
						RemoteMaster rm = rmm.get(sendEntity);
						rm.components.trim();
						for (int j = 0; j < rm.components.size(); j++) {
							if(cvr.getHasChanged(sendEntity, rm.components.get(j)))
								componentQueue.add(rm.components.get(j));
						}
						
						Object[] components = componentQueue.toArray();
						componentQueue.clear();
						
						RemoteMasterUpdate rmu = new RemoteMasterUpdate(sendEntity.id, false, components);
						
						bucket.addData(rmu, false);
					} else {
						RemoteMaster rm = rmm.get(sendEntity);
						rm.components.trim();
						
						for (int j = 0; j < rm.components.size(); j++) {
							cvr.getHasChanged(sendEntity, rm.components.get(j));
						}
						
						RemoteMasterUpdate rmu = new RemoteMasterUpdate(sendEntity.id, true, rm.components.getData());
						
						bucket.addData(rmu, false);
					}
				} else {
					RemoteMasterUpdate rmu = new RemoteMasterUpdate(sendEntity.id, true, new Object[0]); 
					bucket.addData(rmu, false);
				}
			}
			flyweightEntities.clear();
		}
	}
}
