package de.vatterger.entitysystem.factory.server;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.Rectangle;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.entitysystem.components.server.ComponentVersioningRegister;
import de.vatterger.entitysystem.components.server.DataBucket;
import de.vatterger.entitysystem.components.server.EntityAckBucket;
import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.components.shared.Name;
import de.vatterger.entitysystem.components.shared.NetPriorityQueue;
import de.vatterger.entitysystem.components.shared.NetSynchedArea;
import de.vatterger.entitysystem.components.shared.Ping;

public class PlayerFactory {
	
	private PlayerFactory() {}
	
	public static Entity createRTSPlayer(World world, Connection c) {
		return world.createEntity().edit()
			.add(new KryoConnection(c))
			.add(new Ping())
			.add(new DataBucket())
			.add(new EntityAckBucket())
			.add(new ComponentVersioningRegister())
			.add(new Name("Player_"+c))
			.add(new NetSynchedArea(new Rectangle(0,0,0,0)))
			.add(new NetPriorityQueue())
		.getEntity();
	}
}
