package de.vatterger.game;

import com.badlogic.gdx.Game;

public class Client extends Game {
	GameScreen gameScreen;
	@Override
	public void create() {
		gameScreen = new GameScreen();
		setScreen(gameScreen);
	}
}
