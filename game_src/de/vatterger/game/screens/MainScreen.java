
package de.vatterger.game.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;

import de.vatterger.game.screens.manager.ScreenManager;
import de.vatterger.game.ui.listeners.ClickListener;
import de.vatterger.game.ui.listeners.FadeInAction;
import de.vatterger.game.ui.listeners.FadeOutAction;

public class MainScreen extends StageScreen {

	Actor buttonEnterGame;

	@Override
	protected void fillStage(Stage stage, Skin skin) {
		
		Table tableSub0 = new Table(skin);
		tableMain.add(tableSub0).space(Value.percentHeight(0.25f));
		
		buttonEnterGame = new TextButton("button0", skin);
		
		buttonEnterGame.addListener(new ClickListener(buttonEnterGame) {
			@Override
			public void run() {
				buttonEnterGame.setTouchable(Touchable.disabled);
				buttonEnterGame.addAction(new FadeOutAction(0.125f) {
					@Override
					public void run() {
						ScreenManager.setGameScreen();
						buttonEnterGame.clearActions();
						buttonEnterGame.setTouchable(Touchable.enabled);
					}
				});
			}
		});
		
		tableSub0.add(buttonEnterGame).center();
	}

	
	@Override
	public void show() {
		super.show();
		buttonEnterGame.addAction(new FadeInAction(0.125f));
	}
}
