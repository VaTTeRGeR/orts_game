package de.vatterger.entitysystem.processors;

import java.util.HashMap;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;

import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.components.server.EntityAckBucket;
import de.vatterger.entitysystem.handler.network.ServerNetworkHandler;
import de.vatterger.entitysystem.network.FilteredListener;
import de.vatterger.entitysystem.network.KryoNetMessage;
import de.vatterger.entitysystem.network.packets.EntityAckPacket;

@Wire
public class RemoteMasterAckProcessor extends EntityProcessingSystem {

	private ComponentMapper<EntityAckBucket> rebm;
	private ComponentMapper<KryoConnection> kcm;
	
	private HashMap<Connection, EntityAckBucket> updates = new HashMap<Connection, EntityAckBucket>();

	private FilteredListener<EntityAckPacket> listener = new FilteredListener<EntityAckPacket>(EntityAckPacket.class);

	@SuppressWarnings("unchecked")
	public RemoteMasterAckProcessor() {
		super(Aspect.getAspectForAll(KryoConnection.class, EntityAckBucket.class));
	}

	@Override
	protected void initialize() {
		ServerNetworkHandler.instance().addListener(listener);
	}
	
	@Override
	protected void inserted(Entity e) {
		updates.put(kcm.get(e).connection, new EntityAckBucket());
	}
	
	@Override
	protected void removed(Entity e) {
		updates.remove(kcm.get(e).connection);
	}
	
	@Override
	protected void begin() {
		KryoNetMessage<EntityAckPacket> eapkm = null;
		while((eapkm = listener.getNext()) != null) {
			EntityAckBucket eab = updates.get(eapkm.getConnection());
			EntityAckPacket eap = eapkm.getObject();
			
			if(eap.received[0] == 1)
				eab.ids.clear();

			for (int i = 1; i < eap.received.length; i++) {
				eab.ids.add(eap.received[i]);
			}
			Log.info("EAB SIZE: "+eab.ids.size());
			Log.info("EAP[0]  : "+eap.received[0]);
		}
	}

	@Override
	protected void process(Entity e) {
		rebm.get(e).ids = updates.get(kcm.get(e).connection).ids;
	}
}
