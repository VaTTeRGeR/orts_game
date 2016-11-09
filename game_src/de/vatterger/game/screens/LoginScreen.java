
package de.vatterger.game.screens;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;

import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.screens.manager.ScreenManager;
import de.vatterger.game.systems.graphics.FrameTimeDebugRenderSystem;
import de.vatterger.game.ui.listeners.FadeInAction;
import de.vatterger.game.ui.listeners.FadeOutAction;
import de.vatterger.game.ui.listeners.ClickListener;

public class LoginScreen implements Screen {

	World					world;
	Profiler				profiler;

	InputMultiplexer		inputMultiplexer;
	
	Stage					stage;
	Skin					skin;

	public LoginScreen() {
		inputMultiplexer = new InputMultiplexer();
		
		setupStage();
		setupWorld();
	}


	Table tableMain;
	Actor buttonEnterGame;
	
	private void setupStage() {
		skin = new Skin(new FileHandle("assets/visui/assets/uiskin.json"));
		
		stage = new Stage();
		//stage.setDebugAll(true);
		
		tableMain = new Table(skin);
		tableMain.center();
		tableMain.setFillParent(true);
		stage.addActor(tableMain);

		Table tableSub0 = new Table(skin);
		tableSub0.center();
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
		tableSub0.add(buttonEnterGame);

		inputMultiplexer.addProcessor(stage);
	}

	private void setupWorld() {
		WorldConfiguration config = new WorldConfiguration();
		
		config.setSystem(new FrameTimeDebugRenderSystem(profiler = new Profiler("loop")));

		world = new World(config);
	}


	@Override
	public void render(float delta) {
		profiler.start();
		
		Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()) + " - " + (int)((1f/Gdx.graphics.getRawDeltaTime()) + 0.5f));
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		world.setDelta(delta);
		world.process();
		
		stage.act(delta);
		stage.draw();
		
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}

		
		if(Gdx.input.isKeyJustPressed(Keys.F1) && Gdx.graphics.supportsDisplayModeChange()) {
			if(Gdx.graphics.isFullscreen())
				Gdx.graphics.setWindowedMode(640, 480);
			else
				Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		}
}

	@Override
	public void resize(int width, int height) {
		int wv = Metrics.wv = width;
		int hv = Metrics.hv = height;
		
		stage.getViewport().setWorldSize(wv, hv);
		stage.getViewport().update(wv, hv, true);
		tableMain.layout();
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(inputMultiplexer);
		
		buttonEnterGame.addAction(new FadeInAction(0.125f));
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

}
