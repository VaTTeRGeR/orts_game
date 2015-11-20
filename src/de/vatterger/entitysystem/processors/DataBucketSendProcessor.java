package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.components.server.DataBucket;
import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.handler.network.ServerNetworkHandler;
import de.vatterger.entitysystem.network.KryoNetMessage;
import de.vatterger.entitysystem.network.PacketBundle;

public class DataBucketSendProcessor extends EntityProcessingSystem {

	private ComponentMapper<KryoConnection> kcm;
	private ComponentMapper<DataBucket> dbm;
	private ServerNetworkHandler nws = ServerNetworkHandler.instance();

	@SuppressWarnings("unchecked")
	public DataBucketSendProcessor() {
		super(Aspect.getAspectForAll(DataBucket.class));
	}

	@Override
	protected void initialize() {
		kcm = world.getMapper(KryoConnection.class);
		dbm = world.getMapper(DataBucket.class);
	}

	@Override
	protected void process(Entity e) {
		KryoConnection kc = kcm.get(e);
		DataBucket bucket = dbm.get(e);
		Bag<PacketBundle> packets = bucket.getPacketBundles(GameConstants.PACKETSIZE_INTERNET, GameConstants.PACKETS_PER_TICK);
		for (int i = 0; i < packets.size(); i++) {
			nws.sendMessage(new KryoNetMessage(packets.get(i), kc.connection, packets.get(i).getReliable()));
		}
	}
	
	@Override
	protected void dispose() {
	}
}
