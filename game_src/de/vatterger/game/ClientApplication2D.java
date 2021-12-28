package de.vatterger.game;

import java.io.File;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.JsonValue;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.JSONPropertiesHandler;
import de.vatterger.game.screen.manager.ScreenManager;

public class ClientApplication2D extends Game {
	
	@Override
	public void create() {
		
		for (Monitor monitor : Gdx.graphics.getMonitors()) {
			System.out.println(monitor.name + " at (" + monitor.virtualX + ", " + monitor.virtualY + ")");
		}
		
		System.out.println();
		
		System.out.println(Gdx.graphics.getGLVersion().getDebugVersionString());
		
		System.out.println();
		
		System.out.println("Initial Working directory: " + new File("").getAbsolutePath());
		
		System.out.println();
		
		final Runtime runtime = Runtime.getRuntime();
		
		System.out.println("JVM Memory (Xmx): " + (runtime.maxMemory() / 1024 / 1024) + " MB");
		
		System.out.println();
		
		System.out.println("Core Count: " + runtime.availableProcessors());

		System.out.println();
		
		System.out.println("JVM Version: " + Runtime.version());
		
		System.out.println();
		
		IntBuffer texSizeMaxBuffer = BufferUtils.createIntBuffer(32);
		
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, texSizeMaxBuffer.position(0));
		System.out.println("GL20.GL_MAX_TEXTURE_SIZE: " + texSizeMaxBuffer.get());

		System.out.println();

		Gdx.gl.glGetIntegerv(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, texSizeMaxBuffer.position(0));
		System.out.println("GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS: " + texSizeMaxBuffer.get());

		Gdx.gl.glGetIntegerv(GL20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, texSizeMaxBuffer.position(0));
		System.out.println("GL20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS: " + texSizeMaxBuffer.get());
		
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_IMAGE_UNITS, texSizeMaxBuffer.position(0));
		System.out.println("GL20.GL_MAX_TEXTURE_IMAGE_UNITS: " + texSizeMaxBuffer.get());

		// GL20 Extension by NVIDIA needed!
		if(Gdx.graphics.getGLVersion().getVendorString().toLowerCase().contains("nvidia")) {
		
			System.out.println();

			Gdx.gl.glGetIntegerv(0x9048, texSizeMaxBuffer.position(0));
			Gdx.gl.glGetIntegerv(0x9049, texSizeMaxBuffer.position(1));
			
			// Return error code and clears error state.
			/*int error = */Gdx.gl.glGetError();
			
			System.out.println("AVAILABLE / TOTAL GPU MEMORY: " + (texSizeMaxBuffer.get(1) / 1024) + " / " + (texSizeMaxBuffer.get(0) / 1024) + " MB");
		}

		System.out.println();

		ScreenManager.initialize(this);
		
		ScreenManager.setScreen(ScreenManager.MAIN);
		
		//Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
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

	private static JSONPropertiesHandler loadSettings() {
		
		final String[][] defaultValues = new String[16][];
		
		int i = 0;
		
		defaultValues[i++] = new String[] {"width","-1"};
		defaultValues[i++] = new String[] {"height","-1"};
		defaultValues[i++] = new String[] {"fullscreen", "true"};
		defaultValues[i++] = new String[] {"undecorated", "false"};
		defaultValues[i++] = new String[] {"resizable", "true"};
		defaultValues[i++] = new String[] {"vSyncEnabled", "true"};
		defaultValues[i++] = new String[] {"fps", "0"};
		defaultValues[i++] = new String[] {"samples", "0"};
		defaultValues[i++] = new String[] {"useGL30", "true"};
		
		JSONPropertiesHandler settingsHandler = new JSONPropertiesHandler("config/display.json");
		
		JsonValue settingsJsonValue = settingsHandler.getJsonValue();
		
		for (String[] nameValuePair : defaultValues) {
			if(nameValuePair != null && !settingsJsonValue.has(nameValuePair[0])) {
				settingsJsonValue.addChild(nameValuePair[0], new JsonValue(nameValuePair[1]));
			}
		}
		
		settingsHandler.save();
		
		return settingsHandler;
	}
	
	public static void main(String[] args) {
		
		JsonValue settings = loadSettings().getJsonValue();
		
		System.setProperty("org.lwjgl.opengl.Window.undecorated", settings.getString("undecorated"));
		
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
		
		configWindow.width = settings.getInt("width");
		configWindow.height = settings.getInt("height");
		configWindow.fullscreen = settings.getBoolean("fullscreen");
		configWindow.resizable = settings.getBoolean("resizable");
		configWindow.vSyncEnabled = settings.getBoolean("vSyncEnabled");
		configWindow.foregroundFPS = settings.getInt("fps");
		configWindow.backgroundFPS = settings.getInt("fps");
		
		if(configWindow.width <= 0 || configWindow.height <= 0) {
			
			if(configWindow.fullscreen) {
				configWindow.width = desktopMode.width;
				configWindow.height = desktopMode.height;
				
			} else {
				configWindow.width = 640;
				configWindow.height = 480;
			}
		}
		
		configWindow.samples = settings.getInt("samples");
		
		configWindow.useGL30 = settings.getBoolean("useGL30");
		
		if(configWindow.useGL30) {
			configWindow.gles30ContextMajorVersion = 4;
			configWindow.gles30ContextMinorVersion = 3;
		}
		
		configWindow.addIcon("assets/icon32.png", FileType.Internal);
		
		new LwjglApplication(new ClientApplication2D(), configWindow);
	}
}
