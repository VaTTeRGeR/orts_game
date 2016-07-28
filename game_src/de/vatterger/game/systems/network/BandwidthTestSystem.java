package de.vatterger.game.systems.network;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.engine.handler.network.ServerNetworkHandler;
import de.vatterger.game.components.client.ConnectionID;

public class BandwidthTestSystem extends IteratingSystem {

	private ServerNetworkHandler server;
	private ComponentMapper<ConnectionID>		ccm;
	
	public BandwidthTestSystem() {
		super(Aspect.all(ConnectionID.class));
	}
	
	@Override
	protected void initialize() {
		server = ServerNetworkHandler.get();
	}
	
	protected void process(int e) {
		ConnectionID cc = ccm.get(e);
		Connection c  = server.getConnections().get(cc.v);
		//TODO add test code
	}
}
