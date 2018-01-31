package de.vatterger.game;

import java.security.interfaces.RSAPublicKey;

import com.badlogic.gdx.math.MathUtils;

import de.vatterger.engine.handler.encryption.RSAEncryptionManager;
import de.vatterger.engine.handler.encryption.RSAPublicKeyUtility;
import de.vatterger.engine.handler.network.ClientNetworkHandler;
import de.vatterger.engine.network.FilteredListener;
import de.vatterger.engine.network.KryoNetMessage;
import de.vatterger.game.packets.CreateAccountPacket;
import de.vatterger.game.packets.LoginPacket;
import de.vatterger.game.packets.PublicKeyPacket;
import de.vatterger.game.packets.registers.AccountPacketRegister;

public class TestClient {
	public static void main(String[] args) {
		ClientNetworkHandler cnh = ClientNetworkHandler.newInstance("localhost", 26005, new AccountPacketRegister());
		
		if(!ClientNetworkHandler.loaded()) System.exit(1);
		
		FilteredListener<PublicKeyPacket> keyListener = new FilteredListener<PublicKeyPacket>(PublicKeyPacket.class);
		cnh.addListener(keyListener);

		PublicKeyPacket keyPacket = null;
		while(keyPacket == null) {
			KryoNetMessage<PublicKeyPacket> msg = keyListener.getNext();
			
			if(msg != null)
				keyPacket = msg.getObject();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		RSAPublicKey key = RSAPublicKeyUtility.RSAfromBytes(keyPacket.publicKeyBytes);
		
		String name = "user"+MathUtils.random(3);
		String password = name;
		
		cnh.send(new LoginPacket(RSAEncryptionManager.encryptString(name, key), RSAEncryptionManager.encryptString(password, key)), true);
		
		while(true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		//ClientNetworkHandler.dispose();
	}
}
