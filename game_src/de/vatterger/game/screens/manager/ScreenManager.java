package de.vatterger.game.screens.manager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

import de.vatterger.game.screens.GameScreen;
import de.vatterger.game.screens.LoginScreen;

public class ScreenManager {
	
	static Game game = null;
	static Screen loginScreen;
	static Screen gameScreen;

	
	private ScreenManager() {}
	
	public static void initialize(Game game) {
		ScreenManager.game = game;

		loginScreen = new LoginScreen();
		gameScreen = new GameScreen();
	}
	
	public static void setLoginScreen() {
		game.setScreen(loginScreen);
	}

	public static void setGameScreen() {
		game.setScreen(gameScreen);
	}
}
