package de.vatterger.game;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import de.vatterger.engine.handler.network.ServerNetworkHandler;
import de.vatterger.game.packets.registers.AccountPacketRegister;
import de.vatterger.game.systems.network.ChangePasswordSystem;
import de.vatterger.game.systems.network.CheckPasswordSystem;
import de.vatterger.game.systems.network.CreateClientSystem;
import de.vatterger.game.systems.network.CreatePasswordSystem;
import de.vatterger.game.systems.network.HandleClientPacketSystem;
import de.vatterger.game.systems.network.RemoveDeclinedSystem;
import de.vatterger.game.systems.network.RemoveDisconnectedSystem;

public class AccountServerApplication extends ApplicationAdapter {
	
	World world;
	
	@Override
	public void create() {
		ServerNetworkHandler.instance(new AccountPacketRegister(), 26005);
		
		WorldConfigurationBuilder builder = new WorldConfigurationBuilder();
		builder.with(new CreateClientSystem());
		builder.with(new HandleClientPacketSystem());
		builder.with(new CreatePasswordSystem());
		builder.with(new ChangePasswordSystem());
		builder.with(new CheckPasswordSystem());
		builder.with(new RemoveDeclinedSystem());
		builder.with(new RemoveDisconnectedSystem());
		world = new World(builder.build());
	}
	
	@Override
	public void render() {
		world.setDelta(Gdx.graphics.getDeltaTime());
		world.process();
	}

	public static void main(String[] args) {
		HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
		config.renderInterval = 1f/5f;
		config.preferencesDirectory = "settings_accountsserver/";
		new HeadlessApplication(new AccountServerApplication(), config);
	}
}
