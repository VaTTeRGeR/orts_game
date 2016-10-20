
package de.vatterger.game.screen;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.vatterger.engine.camera.RTSCameraController2D;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.screen.manager.ScreenManager;
import de.vatterger.game.systems.graphics.FrameTimeDebugRenderSystem;

public class LoginScreen implements Screen {

	World					world;
	Profiler				profiler;

	Camera					camera;
	Viewport				viewport;
	SpriteBatch				spriteBatch;
	RTSCameraController2D	camController;
	InputMultiplexer		inputMultiplexer;
	
	Stage					stage;
	Skin					skin;

	public LoginScreen() {
		inputMultiplexer = new InputMultiplexer();
		
		setupCamera();
		setupSpriteBatch();
		setupStage();
		setupWorld();
	}

	private void setupSpriteBatch() {
		spriteBatch = new SpriteBatch(4096);
	}
	
	private void setupCamera() {
		camera = new OrthographicCamera();
		viewport = new ScalingViewport(Scaling.fit, Metrics.ww , Metrics.hw, camera);
	}
	
	private void setupStage() {
		skin = new Skin(new FileHandle("assets/visui/assets/uiskin.json"));
		
		stage = new Stage(viewport, spriteBatch);
		
		Button button = new Button(skin);
		button.setFillParent(true);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if(Gdx.input.isTouched()) {
					ScreenManager.setGameScreen();
					button.removeListener(this);
				}
				return true;
			}
		});
		
		stage.getRoot().addActor(button);

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
		
		stage.draw();
		
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
	}

	@Override
	public void resize(int width, int height) {
		Metrics.wv = width;
		Metrics.hv = height;
		Metrics.ww = Metrics.wv * Metrics.mpp;
		Metrics.hw = Metrics.hv * Metrics.mpp;
		
		viewport.setWorldSize(Metrics.ww , Metrics.hw);
		viewport.update(Metrics.wv, Metrics.hv, false);

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
		spriteBatch.dispose();
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}
}
