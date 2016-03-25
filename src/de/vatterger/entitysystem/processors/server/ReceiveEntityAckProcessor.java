package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.server.EntityAckBucket;
import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.handler.network.ServerNetworkHandler;
import de.vatterger.entitysystem.network.FilteredListener;
import de.vatterger.entitysystem.network.KryoNetMessage;
import de.vatterger.entitysystem.network.packets.client.EntityAckPacket;

public class ReceiveEntityAckProcessor extends EntityProcessingSystem {

	private ComponentMapper<EntityAckBucket> eabm;
	private ComponentMapper<KryoConnection> kcm;
	
	private FilteredListener<EntityAckPacket> listener = new FilteredListener<EntityAckPacket>(EntityAckPacket.class);

	public ReceiveEntityAckProcessor() {
		super(Aspect.all(KryoConnection.class, EntityAckBucket.class));
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

			/*System.out.print("EAP {"+eap.received[0]);
			for (int i = 1; i < eap.received.length; i++) {
				System.out.print(","+eap.received[i]);
			}
			System.out.print("}\n");*/

			if(eap.received[0] == 1)
				eab.ids.clear();

			for (int i = 1; i < eap.received.length; i++) {
				eab.ids.add(eap.received[i]);
			}
		}
		//System.out.println("EAB "+eab.ids.toString());
	}
}
