package de.vatterger.game.systems.network;

import com.artemis.BaseSystem;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.engine.handler.network.ServerNetworkHandler;
import de.vatterger.game.components.client.ClientNotLoggedIn;
import de.vatterger.game.components.client.ClientNotRegistered;
import de.vatterger.game.components.client.ConnectionID;

public class ConnectionCreateClientSystem extends BaseSystem {
	
	ServerNetworkHandler snh;
	
	@Override
	protected void initialize() {
		snh = ServerNetworkHandler.get();
	}
	
	@Override
	protected void processSystem() {
		Connection connection = null;
		while((connection = snh.getNextConnected()) != null) {
			world.edit(world.create()).
			add(new ConnectionID(connection.getID())).
			add(new ClientNotRegistered()).
			add(new ClientNotLoggedIn());
		}
	}

	@Override
	protected void dispose() {
		ServerNetworkHandler.dispose();
	}
}
