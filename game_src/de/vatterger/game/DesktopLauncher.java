package de.vatterger.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

public class DesktopLauncher extends ApplicationAdapter {
	public static void main(String[] arg) {
			LwjglApplicationConfiguration configWindow = new LwjglApplicationConfiguration();
			configWindow.width = 800;
			configWindow.height = 600;
			configWindow.samples = 4;
			configWindow.vSyncEnabled = false;
			configWindow.resizable = true;
			configWindow.title = "ORTS";
			configWindow.fullscreen = false;
			configWindow.initialBackgroundColor = Color.BLACK;
			configWindow.allowSoftwareMode = false;
			configWindow.addIcon("icon32.png", FileType.Internal);
			configWindow.backgroundFPS = 30;
			configWindow.foregroundFPS = 120;
			new LwjglApplication(new Client(), configWindow);
	}
}
