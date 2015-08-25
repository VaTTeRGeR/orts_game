package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.components.ClientConnection;
import de.vatterger.entitysystem.components.DataBucket;
import de.vatterger.entitysystem.netservice.NetworkService;
import de.vatterger.entitysystem.networkmessages.PacketBundle;

public class DataBucketSendProcessor extends EntityProcessingSystem {

	private ComponentMapper<ClientConnection> kcm;
	private ComponentMapper<DataBucket> dbm;

	@SuppressWarnings("unchecked")
	public DataBucketSendProcessor() {
		super(Aspect.getAspectForAll(DataBucket.class));
	}

	@Override
	protected void initialize() {
		kcm = world.getMapper(ClientConnection.class);
		dbm = world.getMapper(DataBucket.class);
		NetworkService.instance();
	}

	@Override
	protected void process(Entity e) {
		ClientConnection kc = kcm.get(e);
		DataBucket bucket = dbm.get(e);
		Bag<PacketBundle> packets = bucket.getPacketBundles(1300, 128);
		for (int i = 0; i < packets.size(); i++) {
			kc.connection.sendUDP(packets.get(i));
		}
	}
	
	@Override
	protected void dispose() {
		NetworkService.dispose();
	}
}
