package de.vatterger.game.systems.network;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.engine.handler.network.ServerNetworkHandler;
import de.vatterger.game.components.client.ClientConnection;

public class ConnectionFilterClientSystem extends IteratingSystem {
	
	private ServerNetworkHandler snh;
	
	private ComponentMapper<ClientConnection> ccm;
	
	public ConnectionFilterClientSystem() {
		super(Aspect.all(ClientConnection.class));
	}

	@Override
	protected void initialize() {
		snh = ServerNetworkHandler.get();
	}

	@Override
	protected void process(int e) {
		Connection c = snh.getConnection(ccm.get(e).v);
		if(!c.isConnected()) {
			c.close();
			world.delete(e);
		}
	}
	
	@Override
	protected void dispose() {
		ServerNetworkHandler.dispose();
	}
}
