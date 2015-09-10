package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.components.ClientConnection;
import de.vatterger.entitysystem.components.DataBucket;
import de.vatterger.entitysystem.netservice.MessageOut;
import de.vatterger.entitysystem.netservice.NetworkService;
import de.vatterger.entitysystem.networkmessages.PacketBundle;
import de.vatterger.entitysystem.util.Constants;
import de.vatterger.entitysystem.util.Functions;

public class DataBucketSendProcessor extends EntityProcessingSystem {

	private ComponentMapper<ClientConnection> kcm;
	private ComponentMapper<DataBucket> dbm;
	private NetworkService nws = NetworkService.instance();

	@SuppressWarnings("unchecked")
	public DataBucketSendProcessor() {
		super(Aspect.getAspectForAll(DataBucket.class));
	}

	@Override
	protected void initialize() {
		kcm = world.getMapper(ClientConnection.class);
		dbm = world.getMapper(DataBucket.class);
	}

	@Override
	protected void process(Entity e) {
		ClientConnection kc = kcm.get(e);
		DataBucket bucket = dbm.get(e);
		Bag<PacketBundle> packets = bucket.getPacketBundles(Constants.PACKETSIZE_INTERNET, 8);
		for (int i = 0; i < packets.size(); i++) {
			nws.sendMessage(new MessageOut(packets.get(i), kc.connection, false));
		}
	}
	
	@Override
	protected void dispose() {
	}
}
