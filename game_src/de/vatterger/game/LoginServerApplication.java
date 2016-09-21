package de.vatterger.game;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import de.vatterger.game.components.client.CheckPassword;
import de.vatterger.game.components.client.ConnectionID;
import de.vatterger.game.components.client.Name;
import de.vatterger.game.systems.network.ChangePasswordSystem;
import de.vatterger.game.systems.network.CreatePasswordSystem;
import de.vatterger.game.systems.network.LoginClientSystem;
import de.vatterger.game.systems.network.RemoveDeclinedSystem;

public class LoginServerApplication extends ApplicationAdapter {
	
	World world;
	
	@Override
	public void create() {
		WorldConfigurationBuilder builder = new WorldConfigurationBuilder();
		builder.with(new CreatePasswordSystem());
		builder.with(new ChangePasswordSystem());
		builder.with(new LoginClientSystem());
		builder.with(new RemoveDeclinedSystem());
		world = new World(builder.build());
		
		world.edit(world.create()).
		add(new ConnectionID(-1)).
		add(new Name("florian")).
		add(new CheckPassword("pw222"));

		world.edit(world.create()).
		add(new ConnectionID(-1)).
		add(new Name("florian")).
		add(new CheckPassword("pw222"));
	}
	
	@Override
	public void render() {
		world.setDelta(Gdx.graphics.getDeltaTime());
		world.process();
	}

	public static void main(String[] args) {
		HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
		config.renderInterval = 1f/10f;
		config.preferencesDirectory = "settings_ls/";
		new HeadlessApplication(new LoginServerApplication(), config);
	}
}
