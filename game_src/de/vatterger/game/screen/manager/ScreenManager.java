package de.vatterger.game.screen.manager;

import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

import de.vatterger.game.screen.GameScreen;
import de.vatterger.game.screen.MainScreen;
import de.vatterger.game.screen.OptionScreen;

public class ScreenManager {
	
	public static final int MAIN		= 0;
	public static final int SETTINGS	= 1;
	public static final int GAME		= 2;
	
	private static Game game = null;
	
	private static Screen mainScreen;
	private static Screen settingsScreen;
	private static Screen gameScreen;
	
	private static ArrayList<Integer> screenStack;
	
	private ScreenManager() {}
	
	public static void initialize(Game game) {
		
		ScreenManager.game = game;

		mainScreen		= new MainScreen();
		settingsScreen	= new OptionScreen();
		gameScreen		= new GameScreen();
		
		screenStack = new ArrayList<Integer>(8);
	}
	
	public static void popScreen() {
		
		if(screenStack.size() > 1) {
			
			screenStack.remove(screenStack.size() - 1);
			
			applyScreen(screenStack.get(screenStack.size() - 1));
		}
	}
	
	public static void setScreen(int screen) {
		pushScreen(screen, true);
	}
	
	public static void pushScreen(int screen) {
		pushScreen(screen, false);
	}
	
	private static void pushScreen(int screen, boolean clearStack) {
		
		if(clearStack) {
			screenStack.clear();
		}
		
		screenStack.add(screen);
		
		applyScreen(screen);
	}
	
	private static void applyScreen(int screen) {
		
		switch (screen) {
		
		case MAIN:
			game.setScreen(mainScreen);
			break;

		case SETTINGS:
			game.setScreen(settingsScreen);
			break;

		case GAME:
			game.setScreen(gameScreen);
			break;

		default:
			throw new IllegalStateException("Screen with id "+screen+" does not exist.");
		}
	}
}
