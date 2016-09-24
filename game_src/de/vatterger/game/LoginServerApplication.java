package de.vatterger.game;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.client.CheckPassword;
import de.vatterger.game.components.client.CreatePassword;
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

		world.edit(world.create())
		.add(new Name("florian"))
		.add(new CreatePassword("florian"));
	}
	
	@Override
	public void render() {
		world.edit(world.create())
		.add(new Name("florian"))
		.add(new CheckPassword("florian"));
		
		Profiler p = new Profiler("world.process");
		world.setDelta(Gdx.graphics.getDeltaTime());
		world.process();
		p.log();
	}

	public static void main(String[] args) {
		HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
		config.renderInterval = 1f/1f;
		config.preferencesDirectory = "settings_ls/";
		new HeadlessApplication(new LoginServerApplication(), config);
	}
}
