package de.vatterger.entitysystem.application;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

import de.vatterger.entitysystem.application.MainClient;

public class DesktopLauncher {
	public static void main(String[] arg) {
		boolean fullscreen = false;
		if (!fullscreen) {
			LwjglApplicationConfiguration configWindow = new LwjglApplicationConfiguration();
			configWindow.width = 800;
			configWindow.height = 600;
			configWindow.samples = 4;
			configWindow.vSyncEnabled = false;
			configWindow.resizable = true;
			configWindow.title = "NETWORK DEMO";
			configWindow.fullscreen = false;
			configWindow.initialBackgroundColor = Color.BLACK;
			configWindow.allowSoftwareMode = false;
			configWindow.addIcon("icon32.png", FileType.Internal);
			configWindow.backgroundFPS = 30;
			configWindow.foregroundFPS = 120;
			new LwjglApplication(new ModelTestClient(), configWindow);
		} else {
			LwjglApplicationConfiguration configWindowedFullscreen = new LwjglApplicationConfiguration();
			System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
			configWindowedFullscreen.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
			configWindowedFullscreen.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
			configWindowedFullscreen.samples = 4;
			configWindowedFullscreen.vSyncEnabled = false;
			configWindowedFullscreen.fullscreen = true;
			configWindowedFullscreen.initialBackgroundColor = Color.BLACK;
			configWindowedFullscreen.allowSoftwareMode = false;
			configWindowedFullscreen.backgroundFPS = 30;
			configWindowedFullscreen.foregroundFPS = 120;
			new LwjglApplication(new ModelTestClient(), configWindowedFullscreen);
		}
	}
}
