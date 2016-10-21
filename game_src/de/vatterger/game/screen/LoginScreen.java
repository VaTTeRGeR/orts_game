
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
		viewport = new ScalingViewport(Scaling.fit, Metrics.wv, Metrics.hv, camera);
	}
	
	Table tableMain;
	
	private void setupStage() {
		skin = new Skin(new FileHandle("assets/visui/assets/uiskin.json"));
		
		stage = new Stage(viewport, spriteBatch);
		stage.setDebugAll(true);
		
		tableMain = new Table(skin);
		tableMain.center();
		tableMain.setFillParent(true);
		stage.addActor(tableMain);

		Table tableSub0 = new Table(skin);
		tableSub0.center();
		tableMain.add(tableSub0);
		
		tableMain.row();
		
		Table tableSub1 = new Table(skin);
		tableSub1.center();
		tableMain.add(tableSub1);

		tableMain.row();

		Table tableSub2 = new Table(skin);
		tableSub2.center();
		tableMain.add(tableSub2);
		
		tableSub0.add(new TextButton("button1", skin));
		tableSub1.add(new TextButton("button2", skin));
		tableSub2.add(new TextButton("button3", skin));

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
	}

	@Override
	public void resize(int width, int height) {
		Metrics.wv = width;
		Metrics.hv = height;
		
		stage.getViewport().setWorldSize(Metrics.wv, Metrics.hv);;
		stage.getViewport().update(Metrics.wv, Metrics.hv, true);
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
		spriteBatch.dispose();
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}
}
