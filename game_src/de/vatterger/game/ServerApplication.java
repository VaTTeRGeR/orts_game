package de.vatterger.game;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

public class ServerApplication extends ApplicationAdapter {
	
	World world;
	
	@Override
	public void create() {
		super.create();
		WorldConfigurationBuilder builder = new WorldConfigurationBuilder();
		world = new World(builder.build());
	}
	
	@Override
	public void render() {
		super.render();
		world.setDelta(Gdx.graphics.getDeltaTime());
		world.process();
	}

	public static void main(String[] args) {
		HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
		config.renderInterval = 1f/20f;
		config.preferencesDirectory = "settings/";
		new HeadlessApplication(new ServerApplication(), config);
	}
}
