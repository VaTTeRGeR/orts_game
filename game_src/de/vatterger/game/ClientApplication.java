package de.vatterger.game;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

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
		
		configWindow.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
		configWindow.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
		configWindow.samples = 4;
		
		configWindow.vSyncEnabled = true;
		configWindow.resizable = false;
		configWindow.fullscreen = true;
		
		configWindow.addIcon("icon32.png", FileType.Internal);

		new LwjglApplication(new ClientApplication(), configWindow);
	}
}
