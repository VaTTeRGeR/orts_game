package de.vatterger.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

public class ServerApplication extends ApplicationAdapter {
	
	

	public static void main(String[] args) {
		HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
		config.renderInterval = 1f/20f; // 20 fps
		config.preferencesDirectory = "settings/";
		new HeadlessApplication(new ServerApplication(), config);
	}
}
