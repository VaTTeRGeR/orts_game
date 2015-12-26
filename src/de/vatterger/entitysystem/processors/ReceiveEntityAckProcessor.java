package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.components.server.EntityAckBucket;
import de.vatterger.entitysystem.handler.network.ServerNetworkHandler;
import de.vatterger.entitysystem.network.FilteredListener;
import de.vatterger.entitysystem.network.KryoNetMessage;
import de.vatterger.entitysystem.network.packets.EntityAckPacket;

@Wire
public class ReceiveEntityAckProcessor extends EntityProcessingSystem {

	private ComponentMapper<EntityAckBucket> eabm;
	private ComponentMapper<KryoConnection> kcm;
	
	private FilteredListener<EntityAckPacket> listener = new FilteredListener<EntityAckPacket>(EntityAckPacket.class);

	@SuppressWarnings("unchecked")
	public ReceiveEntityAckProcessor() {
		super(Aspect.getAspectForAll(KryoConnection.class, EntityAckBucket.class));
	}

	@Override
	protected void initialize() {
		ServerNetworkHandler.instance().addListener(listener);
	}
		
	@Override
	protected void process(Entity e) {
		EntityAckBucket eab = eabm.get(e);

		KryoNetMessage<EntityAckPacket> knmeap = null;
		while((knmeap = listener.getNext(kcm.get(e).connection)) != null) {
			EntityAckPacket eap = knmeap.getObject();

			if(eap.received[0] == 1)
				eab.ids.clear();

			for (int i = 1; i < eap.received.length; i++) {
				eab.ids.add(eap.received[i]);
			}
		}
		//System.out.println("EAB "+eab.ids.toString());
	}
}
