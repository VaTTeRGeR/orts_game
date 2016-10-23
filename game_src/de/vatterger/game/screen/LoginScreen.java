
package de.vatterger.game.screen;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;

import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.screen.manager.ScreenManager;
import de.vatterger.game.systems.graphics.FrameTimeDebugRenderSystem;

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
		
		tableMain.row();
		
		Table tableSub1 = new Table(skin);
		tableSub1.center();
		tableMain.add(tableSub1).space(Value.percentHeight(0.25f));

		tableMain.row();

		Table tableSub2 = new Table(skin);
		tableSub2.center();
		tableMain.add(tableSub2).space(Value.percentHeight(0.25f));
		
		TextButton button0 = new TextButton("button0", skin);
		button0.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if(Gdx.input.justTouched()) {
					ScreenManager.setGameScreen();
					return true;
				}
				return false;
			}
		});
		TextButton button1 = new TextButton("button1", skin);
		TextButton button2 = new TextButton("button2", skin);
		tableSub0.add(button0);
		tableSub1.add(button1);
		tableSub2.add(button2);

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
		
		System.out.println("RESIZE LOGINSCREEN");
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(inputMultiplexer);

		System.out.println("SHOW LOGINSCREEN");
	}

	@Override
	public void hide() {
		System.out.println("HIDE LOGINSCREEN");
	}

	@Override
	public void dispose() {
		System.out.println("DISPOSE LOGINSCREEN");
		
		stage.dispose();
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}
}
