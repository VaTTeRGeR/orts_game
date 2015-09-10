package de.vatterger.entitysystem.processors;

import java.util.HashMap;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.entitysystem.components.ClientConnection;
import de.vatterger.entitysystem.components.DataBucket;
import de.vatterger.entitysystem.netservice.NetworkService;
import de.vatterger.entitysystem.util.EntityFactory;

public class ConnectionProcessor extends EntityProcessingSystem {

	private ComponentMapper<ClientConnection> kcm;
	private HashMap<Connection, Entity> connectionToPlayerMap = new HashMap<Connection, Entity>();

	@SuppressWarnings("unchecked")
	public ConnectionProcessor() {
		super(Aspect.getAspectForAll(DataBucket.class));
	}

	@Override
	protected void initialize() {
		kcm = world.getMapper(ClientConnection.class);
	}

	@Override
	protected void begin() {
		Connection c;
		while((c = NetworkService.instance().getConnected()) != null) {
			connectionToPlayerMap.put(c, EntityFactory.createPlayer(world, c));
		}
		while((c = NetworkService.instance().getDisconnected()) != null) {
			if(connectionToPlayerMap.containsKey(c))
				connectionToPlayerMap.remove(c).deleteFromWorld();
		}
	}
	
	@Override
	protected void process(Entity e) {
		ClientConnection kc = kcm.get(e);
		if(!kc.connection.isConnected()) {
			e.deleteFromWorld();
		}
	}
}
