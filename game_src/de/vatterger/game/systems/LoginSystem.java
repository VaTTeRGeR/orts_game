package de.vatterger.game.systems;

import com.artemis.BaseSystem;

import de.vatterger.engine.handler.network.ServerNetworkHandler;
import de.vatterger.engine.network.FilteredListener;

public class LoginSystem extends BaseSystem {

	//FilteredListener<LoginPacket> loginListener;
	
	@Override
	protected void initialize() {
		super.initialize();
		//ServerNetworkHandler.instance().addListener(loginListener = new FilteredListener<>(LoginPacket.class));
	}
	
	@Override
	protected void processSystem() {
		//LoginPacket packet;
		//if((packet = loginListener.getNext().getObject()) == null) {
			//TODO Add login system
		//}
	}
}
