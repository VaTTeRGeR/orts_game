package de.vatterger.entitysystem.processors.server;

import java.util.HashMap;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.entitysystem.components.server.DataBucket;
import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.factory.server.PlayerFactory;
import de.vatterger.entitysystem.handler.network.ServerNetworkHandler;

public class ConnectionProcessor extends EntityProcessingSystem {

	private ComponentMapper<KryoConnection> kcm;
	private HashMap<Connection, Entity> connectionToPlayerMap = new HashMap<Connection, Entity>();

	public ConnectionProcessor() {
		super(Aspect.all(DataBucket.class));
	}

	@Override
	protected void begin() {
		Connection c;
		while((c = ServerNetworkHandler.instance().getNextConnected()) != null) {
			connectionToPlayerMap.put(c, PlayerFactory.createRTSPlayer(world, c));
		}
		while((c = ServerNetworkHandler.instance().getNextDisconnected()) != null) {
			if(connectionToPlayerMap.containsKey(c))
				connectionToPlayerMap.remove(c).deleteFromWorld();
		}
	}
	
	@Override
	protected void process(Entity e) {
		KryoConnection kc = kcm.get(e);
		if(!kc.connection.isConnected()) {
			e.deleteFromWorld();
			connectionToPlayerMap.remove(kc.connection);
		}
	}
}
