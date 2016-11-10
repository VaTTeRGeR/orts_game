package de.vatterger.game.screens.manager;

import java.util.LinkedList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

import de.vatterger.game.screens.GameScreen;
import de.vatterger.game.screens.MainScreen;
import de.vatterger.game.screens.OptionScreen;

public class ScreenManager {
	
	public static final int MAIN = 0;
	public static final int SETTINGS = 1;
	public static final int GAME = 2;
	
	static Game game = null;
	
	static Screen mainScreen;
	static Screen settingsScreen;
	static Screen gameScreen;
	
	static LinkedList<Integer> screenStack;
	
	private ScreenManager() {}
	
	public static void initialize(Game game) {
		ScreenManager.game = game;

		mainScreen = new MainScreen();
		settingsScreen = new OptionScreen();
		gameScreen = new GameScreen();
		
		screenStack = new LinkedList<Integer>();
	}
	
	public static void popScreen() {
		if(screenStack.size() > 1) {
			screenStack.pop();
			
			applyScreen(screenStack.peek());
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
		
		screenStack.push(screen);
		
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
