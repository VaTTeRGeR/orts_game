package de.vatterger.game;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.game.screen.manager.ScreenManager;

public class ClientApplication2D extends Game {
	@Override
	public void create() {
		
		System.out.println(Gdx.graphics.getGLVersion().getDebugVersionString());
		
		//Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		
		ScreenManager.initialize(this);
		
		ScreenManager.setScreen(ScreenManager.MAIN);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		AtlasHandler.dispose();
	}

	@Override
	public void render() {
		
		//Setting window title causes crashes and significant lag on Ubuntu
		//Gdx.graphics.setTitle("ORTS - " + (int)((1f/Gdx.graphics.getRawDeltaTime()) + 0.5f));
		
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
		
		super.render();
	}

	public static void main(String[] args) {
		
		//System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		
		LwjglApplicationConfiguration configWindow = new LwjglApplicationConfiguration();
		
		DisplayMode[] modes = LwjglApplicationConfiguration.getDisplayModes();
		DisplayMode desktopMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
		
		System.out.println();
		System.out.println("Available display modes");
		
		for (DisplayMode mode : modes) {
			System.out.println(mode.toString());
		}

		System.out.println();
		System.out.println("Desktop mode: " + desktopMode.toString());
		System.out.println();
		
		
		
		configWindow.title = "ORTS";
		
		//configWindow.setFromDisplayMode(desktopMode);
		
		configWindow.width = 640;
		configWindow.height = 480;
		
		configWindow.fullscreen = false;
		configWindow.vSyncEnabled = true;
		
		//configWindow.foregroundFPS = 60;
		configWindow.backgroundFPS = 30;
		
		configWindow.samples = 8;

		
		configWindow.addIcon("assets/icon32.png", FileType.Internal);
		
		new LwjglApplication(new ClientApplication2D(), configWindow);
	}
}
