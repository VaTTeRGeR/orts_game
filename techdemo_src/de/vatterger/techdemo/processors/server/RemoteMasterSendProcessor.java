package de.vatterger.techdemo.processors.server;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;

import de.vatterger.techdemo.components.server.ComponentVersioningRegister;
import de.vatterger.techdemo.components.server.DataBucket;
import de.vatterger.techdemo.components.server.EntityAckBucket;
import de.vatterger.techdemo.components.server.RemoteMaster;
import de.vatterger.techdemo.components.shared.GridMapFlag;
import de.vatterger.techdemo.components.shared.NetPriorityQueue;
import de.vatterger.techdemo.components.shared.NetSynchedArea;
import de.vatterger.techdemo.handler.gridmap.GridMapBitFlag;
import de.vatterger.techdemo.handler.gridmap.GridMapHandler;
import de.vatterger.techdemo.network.packets.server.RemoteMasterUpdate;

@Wire
public class RemoteMasterSendProcessor extends EntityProcessingSystem {

	private ComponentMapper<DataBucket> dbm;
	private ComponentMapper<RemoteMaster> rmm;
	private ComponentMapper<NetSynchedArea> nsam;
	private ComponentMapper<GridMapFlag> fm;
	private ComponentMapper<EntityAckBucket> eabm;
	private ComponentMapper<ComponentVersioningRegister> cvrm;
	
	private Bag<Integer> flyweightEntityBag = new Bag<Integer>(256);
	private Queue<Object> flyweightComponentQueue = new LinkedList<Object>();
	private GridMapBitFlag gmbf_networked = new GridMapBitFlag(GridMapBitFlag.NETWORKED);

	public RemoteMasterSendProcessor() {
		super(Aspect.all(DataBucket.class, NetSynchedArea.class, NetPriorityQueue.class, EntityAckBucket.class, ComponentVersioningRegister.class));
	}

	@Override
	protected void process(Entity e) {
		DataBucket bucket = dbm.get(e);
		NetSynchedArea vf = nsam.get(e);
		EntityAckBucket eab = eabm.get(e);
		ComponentVersioningRegister cvr = cvrm.get(e);

		if (bucket.isEmpty()) { // if network queue is empty
			GridMapHandler.getEntities(gmbf_networked , vf.rect, flyweightEntityBag); // sample entities to send to player
			eab.ids.trim();
			Arrays.sort(eab.ids.getData()); // sort list of ids of entities that are already transmitted
			for (int i = 0; i < flyweightEntityBag.size(); i++) { // one RemoteMasterUpdate per Entity
				Entity sendEntity = world.getEntity(flyweightEntityBag.get(i));
				if (fm.get(sendEntity).flag.isContaining(GridMapBitFlag.ACTIVE)) { // if entity is not yet marked for deletion

					if (!(Arrays.binarySearch(eab.ids.getData(), sendEntity.getId()) < 0) && rmm.get(sendEntity).deltaDelay <= 0f) { // Delta Update if id found in ack-bag
						RemoteMaster rm = rmm.get(sendEntity);

						for (int j = 0; j < rm.components.size(); j++) {
							if (cvr.getHasChanged(sendEntity, rm.components.get(j)))
								flyweightComponentQueue.add(rm.components.get(j));
						}

						Object[] components = flyweightComponentQueue.toArray(new Object[flyweightComponentQueue.size()]);
						flyweightComponentQueue.clear();

						RemoteMasterUpdate rmu = new RemoteMasterUpdate(sendEntity.getId(), false, components);

						bucket.addData(rmu, false);
						//System.out.println("Delta update: "+sendEntity.getId()+", "+components.length+" components");
					} else { // Full Update
						RemoteMaster rm = rmm.get(sendEntity);

						for (int j = 0; j < rm.components.size(); j++) {
							cvr.getHasChanged(sendEntity, rm.components.get(j));
						}

						RemoteMasterUpdate rmu = new RemoteMasterUpdate(sendEntity.getId(), true, rm.components.getData());

						bucket.addData(rmu, false);
						//System.out.println("Full update: "+sendEntity.getId()+", "+rm.components.size()+" components");
					}
				} else { // Entity is Inactive
					RemoteMasterUpdate rmu = new RemoteMasterUpdate(sendEntity.getId(), true, new Object[0]);
					bucket.addData(rmu, false);
				}
			}
			flyweightEntityBag.clear();
		}
	}
}
