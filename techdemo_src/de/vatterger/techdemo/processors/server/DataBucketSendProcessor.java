package de.vatterger.techdemo.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;

import de.vatterger.techdemo.application.GameConstants;
import de.vatterger.techdemo.components.server.DataBucket;
import de.vatterger.techdemo.components.server.KryoConnection;
import de.vatterger.techdemo.network.packets.server.PacketBundle;

public class DataBucketSendProcessor extends EntityProcessingSystem {

	private ComponentMapper<KryoConnection> kcm;
	private ComponentMapper<DataBucket> dbm;

	public DataBucketSendProcessor() {
		super(Aspect.all(DataBucket.class, KryoConnection.class));
	}

	@Override
	protected void process(Entity e) {
		Bag<PacketBundle> packets = dbm.get(e).getPacketBundles(GameConstants.PACKETSIZE_INTERNET, GameConstants.PACKETS_PER_TICK);
		for (int i = 0; i < packets.size(); i++) {
			if(packets.get(i).getReliable()) {
				kcm.get(e).connection.sendTCP(packets.get(i));
			} else {
				kcm.get(e).connection.sendUDP(packets.get(i));
			}
		}
	}
}
