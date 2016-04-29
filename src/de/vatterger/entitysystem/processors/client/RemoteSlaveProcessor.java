package de.vatterger.entitysystem.processors.client;

import static de.vatterger.entitysystem.application.GameConstants.ENTITY_UPDATE_TIMEOUT;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.components.client.RemoteSlave;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.factory.shared.EntityModifyFactory;
import de.vatterger.entitysystem.handler.network.ClientNetworkHandler;
import de.vatterger.entitysystem.network.FilteredListener;
import de.vatterger.entitysystem.network.KryoNetMessage;
import de.vatterger.entitysystem.network.packets.server.RemoteMasterUpdate;

@Wire
public class RemoteSlaveProcessor extends IteratingSystem {

	private ComponentMapper<RemoteSlave>	rsm;
	
	private Bag<RemoteMasterUpdate> updateRegister = new Bag<RemoteMasterUpdate>(1);
	private Bag<Integer> slaveRegister = new Bag<Integer>(1);
	
	private FilteredListener<RemoteMasterUpdate> listener = new FilteredListener<RemoteMasterUpdate>(RemoteMasterUpdate.class);
	
	public RemoteSlaveProcessor() {
		super(Aspect.all(RemoteSlave.class));
	}

	@Override
	protected void initialize() {
		ClientNetworkHandler.instance().addListener(listener);
	}
	
	@Override
	protected void begin() {
		KryoNetMessage<RemoteMasterUpdate> msg;
		while ((msg = listener.getNext()) != null) {
			RemoteMasterUpdate rmu = msg.getObject();
			int id = rmu.id;
			updateRegister.set(id, rmu);
			if (slaveRegister.safeGet(id) == null && rmu.components.length != 0) { // if there is no slave for this master and the update is not empty (= deleted)
				slaveRegister.set(id, world.createEntity().edit().add(new RemoteSlave(id)).getEntity().getId());
			}
		}
	}

	@Override
	protected void process(int e) {
		RemoteSlave rs = rsm.get(e);
		RemoteMasterUpdate rmu = updateRegister.get(rs.masterId);
		Entity ent = world.getEntity(e);
		if (rmu != null) { // there is an update for this slave
			if (rmu.isFullUpdate()) { // it's a full update

				Bag<Component> components = new Bag<Component>(8);
				world.getComponentManager().getComponentsFor(e, components);

				if (rmu.components.length == 0) { // mark the entity for deletion

					EntityModifyFactory.stripComponents(ent); // make this slave an empty entity
					world.edit(e).add(new Inactive()); // add to this slave an inactive component
					slaveRegister.set(rmu.id, null); // remove this slave from the slave register
				
				} else { // fill the slave with the masters components
					
					EntityEdit ed = world.edit(e);
					for (int i = 0; i < rmu.components.length; i++) {
						ed.add((Component) rmu.components[i]);
					}

				}
			} else { // it's a partial update
				EntityEdit edit = world.edit(e);
				for (int i = 0; i < rmu.components.length; i++) {
					edit.add((Component)rmu.components[i]);
				}
			}
			
			//INTERPOLATION_PERIOD_MEASURED = GameUtil.clamp(GameConstants.INTERPOLATION_PERIOD_MIN, rs.lastUpdateDelay, GameConstants.INTERPOLATION_PERIOD_MEASURED*2f);
			//System.out.println(rs.lastUpdateDelay);
			rs.lastUpdateDelay = 0f; // the update timer is reset
			updateRegister.set(rmu.id, null); // the update for this slave is removed from the bag
		} else { // there is no update for this slave
			if(rs.lastUpdateDelay > ENTITY_UPDATE_TIMEOUT) { // the slave is starved of updates
				EntityModifyFactory.stripComponents(ent); // make this slave an empty entity
				world.edit(e).add(new Inactive()); // add to this slave an inactive component
				slaveRegister.set(rs.masterId, null);
			} else if(slaveRegister.get(rs.masterId) == null) { // There is no slave registered for this master
				EntityModifyFactory.stripComponents(ent); // make this slave an empty entity
				world.edit(e).add(new Inactive()); // add to this slave an inactive component
			}
			rs.lastUpdateDelay+=world.getDelta();
		}
	}
}
