package de.vatterger.game.screens.manager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

import de.vatterger.game.screens.GameScreen;
import de.vatterger.game.screens.MainScreen;
import de.vatterger.game.screens.OptionScreen;

public class ScreenManager {
	
	static Game game = null;
	static Screen mainScreen;
	static Screen optionScreen;
	static Screen gameScreen;

	
	private ScreenManager() {}
	
	public static void initialize(Game game) {
		ScreenManager.game = game;

		mainScreen = new MainScreen();
		optionScreen = new OptionScreen();
		gameScreen = new GameScreen();
	}
	
	public static void setMainScreen() {
		game.setScreen(mainScreen);
	}

	public static void setOptionScreen() {
		game.setScreen(optionScreen);
	}

	public static void setGameScreen() {
		game.setScreen(gameScreen);
	}
}
