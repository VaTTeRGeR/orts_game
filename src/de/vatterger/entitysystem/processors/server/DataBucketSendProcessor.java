package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.components.server.DataBucket;
import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.handler.network.ServerNetworkHandler;
import de.vatterger.entitysystem.network.KryoNetMessage;
import de.vatterger.entitysystem.network.packets.PacketBundle;

@Wire
public class DataBucketSendProcessor extends EntityProcessingSystem {

	private ComponentMapper<KryoConnection> kcm;
	private ComponentMapper<DataBucket> dbm;
	private ServerNetworkHandler snh = ServerNetworkHandler.instance();

	@SuppressWarnings("unchecked")
	public DataBucketSendProcessor() {
		super(Aspect.getAspectForAll(DataBucket.class));
	}

	@Override
	protected void process(Entity e) {
		Bag<PacketBundle> packets = dbm.get(e).getPacketBundles(GameConstants.PACKETSIZE_INTERNET, GameConstants.PACKETS_PER_TICK);
		for (int i = 0; i < packets.size(); i++) {
			snh.sendMessage(new KryoNetMessage<PacketBundle>(packets.get(i), kcm.get(e).connection, packets.get(i).getReliable()));
		}
	}
}
