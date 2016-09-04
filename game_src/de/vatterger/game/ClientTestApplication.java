package de.vatterger.game;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import de.vatterger.game.screens.Prototype2DScreen;

public class ClientTestApplication extends Game {
	Screen screen;

	@Override
	public void create() {
		System.out.println(Gdx.graphics.getGLVersion().getDebugVersionString());
		screen = new Prototype2DScreen();
		setScreen(screen);
	}
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration configWindow = new LwjglApplicationConfiguration();
		
		configWindow.title = "ORTS";
		
		configWindow.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
		configWindow.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
		configWindow.width = 640;
		configWindow.height = 480;
		configWindow.samples = 0;
		
		configWindow.vSyncEnabled = false;
		configWindow.resizable = true;
		configWindow.fullscreen = false;
		
		configWindow.foregroundFPS = 120;
		configWindow.backgroundFPS = 30;
		
		configWindow.addIcon("icon32.png", FileType.Internal);

		new LwjglApplication(new ClientTestApplication(), configWindow);
	}
}
