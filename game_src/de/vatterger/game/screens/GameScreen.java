
package de.vatterger.game.screens;

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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.vatterger.engine.camera.RTSCameraController2D;
import de.vatterger.engine.handler.unit.UnitHandler;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.SpriteLayer;
import de.vatterger.game.screens.manager.ScreenManager;
import de.vatterger.game.systems.gameplay.CreateTestEntitySystem;
import de.vatterger.game.systems.gameplay.MoveByVelocitySystem;
import de.vatterger.game.systems.gameplay.RemoveEntitySystem;
import de.vatterger.game.systems.gameplay.RemoveTimedSystem;
import de.vatterger.game.systems.graphics.CullingSystem;
import de.vatterger.game.systems.graphics.FrameTimeDebugRenderSystem;
import de.vatterger.game.systems.graphics.ParentSystem;
import de.vatterger.game.systems.graphics.SpriteRenderSystem;
import de.vatterger.game.systems.graphics.TerrainRenderSystem;
import de.vatterger.game.systems.graphics.TracerHitSystem;
import de.vatterger.game.systems.graphics.TurretRotateToMouseSystem;
import de.vatterger.game.ui.listeners.ClickListener;
import de.vatterger.game.ui.listeners.FadeInAction;
import de.vatterger.game.ui.listeners.FadeOutAction;

public class GameScreen implements Screen {

	World					world;
	Profiler				profiler;

	Camera					camera;
	Viewport				viewport;
	SpriteBatch				spriteBatch;
	RTSCameraController2D	camController;
	InputMultiplexer		inputMultiplexer;

	Stage					stage;
	Skin					skin;

	public GameScreen() {
		inputMultiplexer = new InputMultiplexer();
		setupCamera();
		setupSpriteBatch();
		setupWorld();
		spawnUnits();
		setupStage();
	}

	private void setupCamera() {
		camera = new OrthographicCamera();
		camera.near = 0f;
		camera.far = 1f;
		viewport = new ScalingViewport(Scaling.fit, Metrics.ww , Metrics.hw, camera);
		camController = new RTSCameraController2D(viewport, this);
		inputMultiplexer.addProcessor(camController);
	}

	private void setupSpriteBatch() {
		spriteBatch = new SpriteBatch(4096);
	}
	
	private void setupWorld() {
		WorldConfiguration config = new WorldConfiguration();
		
		config.setSystem(new CreateTestEntitySystem(camera));
		config.setSystem(new RemoveEntitySystem(camera));
		config.setSystem(new RemoveTimedSystem());
		config.setSystem(new ParentSystem());

		config.setSystem(new TurretRotateToMouseSystem(camera));
		config.setSystem(new MoveByVelocitySystem());
		config.setSystem(new TracerHitSystem());

		config.setSystem(new CullingSystem(camera));
		config.setSystem(new TerrainRenderSystem(camera));
		config.setSystem(new SpriteRenderSystem(camera));
		config.setSystem(new FrameTimeDebugRenderSystem(profiler = new Profiler("loop")));

		world = new World(config);
	}

	private void spawnUnits() {
		//UnitHandler.createGroundTile("tile_grass", new Vector3(0f, 0f, 0f), world);
		//UnitHandler.createGroundTile("tile_grass", new Vector3(40f, 0f, 0f), world);
		//UnitHandler.createGroundTile("tile_grass", new Vector3(0f, 40f, 0f), world);
		//UnitHandler.createGroundTile("tile_grass_ll", new Vector3(40f, 40f, 0f), SpriteLayer.GROUND1, world);
		//for (int i = 0; i < 1; i++) {
		//	for (int j = 0; j < 1; j++) {
		//		UnitHandler.createRandomTerrainTile(new Vector3(i*100f, j*100f, 0f), world);
		//	}
		//}

		float m[][] = {
				{0.5f,0f,1f,1f,0f,0.5f},
				{0f,0f,1f,0f,1f,0f},
				{1f,0f,0.5f,0.25f,0f,1f},
				{1f,1f,1f,0f,0f,1f},
				{0f,1f,0f,0f,1f,0f},
				{0.5f,0f,1f,1f,0f,0.5f},
		};
		
		UnitHandler.createTerrainTile(m,new Vector3(0f, 0f, 0f), world);
		UnitHandler.createTerrainTile(m,new Vector3(50f, 0f, 0f), world);
		UnitHandler.createTerrainTile(m,new Vector3(0f, 50f, 0f), world);
		UnitHandler.createTerrainTile(m,new Vector3(50f, 50f, 0f), world);
		
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 11; j++) {
				UnitHandler.createInfatry("soldier", new Vector3(10f*i, 10f*j, 0f), world);
			}
		}
		
		for (int i = 0; i < 5; i++) {
			UnitHandler.createInfatry("soldier", new Vector3(2f*i-4f, -4f, 0f), world);
		}

		for (int i = 0; i < 5; i++) {
			UnitHandler.createInfatry("soldier", new Vector3(2f*i-4.25f, -3f, 0f), world);
		}

		UnitHandler.createTank("pz1b", new Vector3(10f, 5f, 0f), world);
	}
	
	Table tableMain;
	Button buttonExitGame;
	Button buttonTestGame;
	
	private void setupStage() {
		skin = new Skin(new FileHandle("assets/visui/assets/uiskin.json"));
		
		stage = new Stage();
		
		tableMain = new Table(skin);
		tableMain.setFillParent(true);
		tableMain.top();
		stage.addActor(tableMain);

		Table tableSub0 = new Table(skin);
		tableMain.add(tableSub0).expandX().fillX().right();

		buttonTestGame = new TextButton("TEST", skin);
		buttonTestGame.setDisabled(true);

		buttonExitGame = new TextButton("EXIT", skin);
		buttonExitGame.addListener(new ClickListener(buttonExitGame) {
			@Override
			public void run() {
				buttonExitGame.setTouchable(Touchable.disabled);
				buttonExitGame.addAction(new FadeOutAction(0.125f) {
					@Override
					public void run(){
						ScreenManager.setScreen(ScreenManager.MAIN);
						buttonExitGame.clearActions();
						buttonExitGame.setTouchable(Touchable.enabled);
					}
				});
				buttonTestGame.addAction(new FadeOutAction(0.125f));
			}
		});

		tableSub0.add(buttonTestGame).padTop(4).padLeft(4).space(4).expandX().fillX();
		tableSub0.add(buttonExitGame).padTop(4).padRight(4).space(4).width(50);

		inputMultiplexer.addProcessor(stage);
	}

	@Override
	public void render(float delta) {
		profiler.start();
		
		camController.update();
		
		Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()) + " - " + (int)((1f/Gdx.graphics.getRawDeltaTime()) + 0.5f));
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		world.setDelta(delta);
		world.process();
		
		stage.act(delta);
		stage.draw();

		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			ScreenManager.pushScreen(ScreenManager.SETTINGS);
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
		Metrics.wv = width;
		Metrics.hv = height;
		
		Metrics.ww = Metrics.wv * Metrics.mpp * camController.getZoom();
		Metrics.hw = Metrics.hv * Metrics.mpp * camController.getZoom();
		
		viewport.setWorldSize(Metrics.ww , Metrics.hw);
		viewport.update(Metrics.wv, Metrics.hv, false);

		stage.getViewport().setWorldSize(Metrics.wv, Metrics.hv);
		stage.getViewport().update(Metrics.wv, Metrics.hv, true);
		tableMain.validate();
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(inputMultiplexer);
		
		buttonTestGame.addAction(new FadeInAction(0.125f));
		buttonExitGame.addAction(new FadeInAction(0.125f));
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		stage.dispose();
	}

	@Override
	public void hide() {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}
}
