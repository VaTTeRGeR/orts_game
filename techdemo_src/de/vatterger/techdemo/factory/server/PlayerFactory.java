package de.vatterger.techdemo.factory.server;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.Rectangle;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.techdemo.components.server.ComponentVersioningRegister;
import de.vatterger.techdemo.components.server.DataBucket;
import de.vatterger.techdemo.components.server.EntityAckBucket;
import de.vatterger.techdemo.components.server.KryoConnection;
import de.vatterger.techdemo.components.shared.Name;
import de.vatterger.techdemo.components.shared.NetPriorityQueue;
import de.vatterger.techdemo.components.shared.NetSynchedArea;
import de.vatterger.techdemo.components.shared.Ping;

public class PlayerFactory {
	
	private PlayerFactory() {}
	
	public static Entity createPlayer(World world, Connection c) {
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
