package de.vatterger.entitysystem.processors;

import java.util.HashMap;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.entitysystem.EntityFactory;
import de.vatterger.entitysystem.components.server.DataBucket;
import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.network.ServerNetworkService;

public class ConnectionProcessor extends EntityProcessingSystem {

	private ComponentMapper<KryoConnection> kcm;
	private HashMap<Connection, Entity> connectionToPlayerMap = new HashMap<Connection, Entity>();

	@SuppressWarnings("unchecked")
	public ConnectionProcessor() {
		super(Aspect.getAspectForAll(DataBucket.class));
	}

	@Override
	protected void initialize() {
		kcm = world.getMapper(KryoConnection.class);
	}

	@Override
	protected void begin() {
		Connection c;
		while((c = ServerNetworkService.instance().getNextConnected()) != null) {
			connectionToPlayerMap.put(c, EntityFactory.createPlayer(world, c));
		}
		while((c = ServerNetworkService.instance().getNextDisconnected()) != null) {
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
