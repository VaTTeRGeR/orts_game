
package de.vatterger.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;

import de.vatterger.game.screens.manager.ScreenManager;
import de.vatterger.game.ui.ClickListener;
import de.vatterger.game.ui.FadeInAction;
import de.vatterger.game.ui.FadeOutAction;

public class OptionScreen extends StageScreen {

	TextButton buttonFullscreen;
	TextButton buttonDesktop;
	TextButton buttonMainMenu;
	TextButton buttonBack;

	@Override
	protected void fillStage(Stage stage, Skin skin) {
		
		Table tableSub0 = new Table();
		
		Stack stack = new Stack(tableSub0);
		
		tableMain.add(stack);

		buttonFullscreen = new TextButton("Toggle Fullscreen", skin);
		
		buttonFullscreen.addListener(new ClickListener() {
			@Override
			public void run() {
				buttonFullscreen.setTouchable(Touchable.disabled);
				buttonFullscreen.addAction(new FadeOutAction(0.125f) {
					@Override
					public void run() {
						buttonFullscreen.clearActions();
						buttonFullscreen.setTouchable(Touchable.enabled);
						buttonFullscreen.addAction(new FadeInAction(0.125f));
						
						if(Gdx.graphics.isFullscreen())
							Gdx.graphics.setWindowedMode(640, 480);
						else
							Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
					}
				});
			}
		});
		
		buttonDesktop = new TextButton("Exit to Desktop", skin);
		
		buttonDesktop.addListener(new ClickListener() {
			@Override
			public void run() {
				Gdx.app.exit();
			}
		});
		
		buttonMainMenu = new TextButton("Exit to Main Menu", skin);
		
		buttonMainMenu.addListener(new ClickListener() {
			@Override
			public void run() {
				buttonMainMenu.setTouchable(Touchable.disabled);
				buttonMainMenu.addAction(new FadeOutAction(0.125f) {
					@Override
					public void run() {
						buttonMainMenu.clearActions();
						buttonMainMenu.setTouchable(Touchable.enabled);

						ScreenManager.setScreen(ScreenManager.MAIN);
					}
				});
			}
		});
		
		buttonBack = new TextButton("Back to the Game", skin);
		
		buttonBack.addListener(new ClickListener() {
			@Override
			public void run() {
				listenerActor.setTouchable(Touchable.disabled);
				listenerActor.addAction(new FadeOutAction(0.125f) {
					@Override
					public void run() {
						listenerActor.clearActions();
						listenerActor.setTouchable(Touchable.enabled);

						ScreenManager.popScreen();
					}
				});
			}
		});
		
		tableSub0.add(buttonFullscreen).space(Value.percentHeight(0.25f)).center().fillX().row();
		tableSub0.add(buttonDesktop).space(Value.percentHeight(0.25f)).center().fillX().row();
		tableSub0.add(buttonMainMenu).space(Value.percentHeight(0.25f)).center().fillX().row();
		tableSub0.add(buttonBack).space(Value.percentHeight(0.25f)).center().fillX().row();
	}

	
	@Override
	public void render(float delta) {		
		if(Gdx.graphics.isFullscreen())
			buttonFullscreen.setText("Set 640x480");
		else
			buttonFullscreen.setText("Set Fullscreen");

		super.render(delta);

		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			ScreenManager.popScreen();
		}
	}
	
	@Override
	public void show() {
		super.show();
		buttonFullscreen.addAction(new FadeInAction(0.125f));
		buttonDesktop.addAction(new FadeInAction(0.125f));
		buttonMainMenu.addAction(new FadeInAction(0.125f));
		buttonBack.addAction(new FadeInAction(0.125f));
	}
}
