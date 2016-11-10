
package de.vatterger.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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

public class OptionScreen extends StageScreen {

	Actor buttonBack;
	Actor buttonFullscreen;

	@Override
	protected void fillStage(Stage stage, Skin skin) {
		
		Table tableSub0 = new Table(skin);
		tableMain.add(tableSub0).space(Value.percentHeight(0.25f));

		buttonFullscreen = new TextButton("Toggle Fullscreen", skin);
		
		buttonFullscreen.addListener(new ClickListener(buttonFullscreen) {
			@Override
			public void run() {
				//tableSub0.setTouchable(Touchable.disabled);
				buttonFullscreen.addAction(new FadeOutAction(0.125f) {
					@Override
					public void run() {
						buttonFullscreen.clearActions();
						//tableSub0.setTouchable(Touchable.enabled);
						
						if(Gdx.graphics.isFullscreen())
							Gdx.graphics.setWindowedMode(640, 480);
						else
							Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
					}
				});
			}
		});
		
		buttonBack = new TextButton("back", skin);
		
		buttonBack.addListener(new ClickListener(buttonBack) {
			@Override
			public void run() {
				//tableSub0.setTouchable(Touchable.disabled);
				buttonBack.addAction(new FadeOutAction(0.125f) {
					@Override
					public void run() {
						buttonBack.clearActions();
						//tableSub0.setTouchable(Touchable.enabled);

						ScreenManager.setGameScreen();
					}
				});
			}
		});
		
		tableSub0.add(buttonFullscreen).space(Value.percentHeight(0.25f)).center().row();
		tableSub0.add(buttonBack).center().row();
	}

	
	@Override
	public void render(float delta) {
		super.render(delta);

		if(Gdx.input.isKeyJustPressed(Keys.NUM_1)) {
			ScreenManager.setGameScreen();
		}
		
	}
	
	@Override
	public void show() {
		super.show();
		buttonBack.addAction(new FadeInAction(0.125f));
		buttonFullscreen.addAction(new FadeInAction(0.125f));
	}
}
