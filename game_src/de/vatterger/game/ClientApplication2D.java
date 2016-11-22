package de.vatterger.game;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.game.screens.manager.ScreenManager;

public class ClientApplication2D extends Game {
	@Override
	public void create() {
		System.out.println(Gdx.graphics.getGLVersion().getDebugVersionString());
		
		ScreenManager.initialize(this);
		
		ScreenManager.setScreen(ScreenManager.MAIN);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		AtlasHandler.dispose();
	}
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration configWindow = new LwjglApplicationConfiguration();
		
		configWindow.title = "ORTS";
		
		configWindow.fullscreen = false;
		configWindow.vSyncEnabled = false;
		configWindow.resizable = false;

		configWindow.width = 640;
		configWindow.height = 480;
		
		configWindow.foregroundFPS = 60;
		configWindow.backgroundFPS = 30;
		
		configWindow.addIcon("icon32.png", FileType.Internal);

		new LwjglApplication(new ClientApplication2D(), configWindow);
	}
}
