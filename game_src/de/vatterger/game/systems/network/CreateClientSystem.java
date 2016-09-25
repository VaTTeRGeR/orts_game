package de.vatterger.game.systems.network;

import com.artemis.BaseSystem;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.engine.handler.encryption.RSADecryptionManager;
import de.vatterger.engine.handler.encryption.RSAPublicKeyUtility;
import de.vatterger.engine.handler.network.ServerNetworkHandler;
import de.vatterger.game.components.client.ClientNotLoggedIn;
import de.vatterger.game.components.client.ClientNotRegistered;
import de.vatterger.game.components.client.ConnectionID;
import de.vatterger.game.packets.PublicKeyPacket;

public class CreateClientSystem extends BaseSystem {
	
	ServerNetworkHandler snh;
	
	@Override
	protected void initialize() {
		snh = ServerNetworkHandler.get(26005);
	}
	
	@Override
	protected void processSystem() {
		Connection connection = null;
		while((connection = snh.getNextConnected()) != null) {
			world.edit(world.create()).
			add(new ConnectionID(connection.getID())).
			add(new ClientNotRegistered()).
			add(new ClientNotLoggedIn());
			
			System.out.println("Created new Client "+connection.getID());

			byte[] rsaPublicKeyBytes = RSAPublicKeyUtility.RSAtoBytes(RSADecryptionManager.getRSAPublicKey());
			connection.sendTCP(new PublicKeyPacket(rsaPublicKeyBytes));

			System.out.println("Sent RSA key to Client "+connection.getID());
		}
	}

	@Override
	protected void dispose() {
		ServerNetworkHandler.dispose(26005);
	}
}
