package de.vatterger.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

import de.vatterger.game.screens.GameScreen;

public class ClientApplication extends Game {
	GameScreen gameScreen;
	@Override
	public void create() {
		System.out.println(Gdx.graphics.getGLVersion().getDebugVersionString());
		gameScreen = new GameScreen();
		setScreen(gameScreen);
	}
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration configWindow = new LwjglApplicationConfiguration();
		configWindow.title = "ORTS";

		configWindow.width = 1680;
		configWindow.height = 1050;
		configWindow.samples = 4;

		configWindow.vSyncEnabled = true;
		configWindow.resizable = true;
		configWindow.fullscreen = true;
		configWindow.initialBackgroundColor = Color.BLACK;

		configWindow.backgroundFPS = 30;
		configWindow.foregroundFPS = 120;
		
		configWindow.addIcon("icon32.png", FileType.Internal);

		new LwjglApplication(new ClientApplication(), configWindow);
	}
}
