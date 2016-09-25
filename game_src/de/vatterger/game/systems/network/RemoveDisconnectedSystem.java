package de.vatterger.game.systems.network;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.engine.handler.network.ServerNetworkHandler;
import de.vatterger.game.components.client.ConnectionID;

public class RemoveDisconnectedSystem extends IteratingSystem {
	
	private ServerNetworkHandler snh;
	
	private ComponentMapper<ConnectionID> ccm;
	
	public RemoveDisconnectedSystem() {
		super(Aspect.all(ConnectionID.class));
	}

	@Override
	protected void initialize() {
		snh = ServerNetworkHandler.get(26005);
	}

	@Override
	protected void process(int e) {
		Connection c = snh.getConnection(ccm.get(e).cid);
		if(c == null) {
			world.delete(e);
		} else if(!c.isConnected()) {
			c.close();
			world.delete(e);
		}
	}
	
	@Override
	protected void dispose() {
		ServerNetworkHandler.dispose(26005);
	}
}
