
package de.vatterger.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Scaling;

import de.vatterger.game.screens.manager.ScreenManager;
import de.vatterger.game.ui.listeners.ClickListener;
import de.vatterger.game.ui.listeners.FadeInAction;
import de.vatterger.game.ui.listeners.FadeOutAction;

public class MainScreen extends StageScreen {

	Actor buttonEnterGame;
	Actor buttonExitGame;
	
	@Override
	protected void fillStage(Stage stage, Skin skin) {
		
		//stage.setDebugAll(true);
		
		Image background = new Image(new Texture("assets/background/map_background.png"));
		background.setScaling(Scaling.none);
		
		buttonEnterGame = new TextButton("Begin", skin);
		
		buttonEnterGame.addListener(new ClickListener(buttonEnterGame) {
			@Override
			public void run() {
				buttonEnterGame.setTouchable(Touchable.disabled);
				buttonEnterGame.addAction(new FadeOutAction(0.125f) {
					@Override
					public void run() {
						ScreenManager.setScreen(ScreenManager.GAME);
						buttonEnterGame.clearActions();
						buttonEnterGame.setTouchable(Touchable.enabled);
					}
				});
			}
		});
		
		buttonExitGame = new TextButton("Exit", skin);
		
		buttonExitGame.addListener(new ClickListener(buttonExitGame) {
			@Override
			public void run() {
				Gdx.app.exit();
			}
		});
		
		Table tableSub0 = new Table(skin);
		
		tableSub0.add(buttonEnterGame).space(Value.percentHeight(0.25f)).center().row();
		tableSub0.add(buttonExitGame).space(Value.percentHeight(0.25f)).center().row();

		Stack stack = new Stack(background, tableSub0);
		
		tableMain.add(stack);
		
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			ScreenManager.popScreen();
		}
		
	}
	
	@Override
	public void show() {
		super.show();
		buttonEnterGame.addAction(new FadeInAction(0.125f));
		buttonExitGame.addAction(new FadeInAction(0.125f));
	}
}