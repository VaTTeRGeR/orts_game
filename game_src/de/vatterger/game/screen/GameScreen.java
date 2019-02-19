
package de.vatterger.game.screen;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.vatterger.engine.camera.RTSCameraController2D;
import de.vatterger.engine.handler.unit.UnitHandlerJSON;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.screen.manager.ScreenManager;
import de.vatterger.game.systems.gameplay.AssignRandomPathsSystem;
import de.vatterger.game.systems.gameplay.CreateTestEntitySystem;
import de.vatterger.game.systems.gameplay.FadeSpriteSystem;
import de.vatterger.game.systems.gameplay.MaintainCollisionMapSystem;
import de.vatterger.game.systems.gameplay.MoveAlongPathSystem;
import de.vatterger.game.systems.gameplay.MoveByVelocitySystem;
import de.vatterger.game.systems.gameplay.RemoveEntitySystem;
import de.vatterger.game.systems.gameplay.RemoveTimedSystem;
import de.vatterger.game.systems.gameplay.TimeSystem;
import de.vatterger.game.systems.graphics.AnimatedSpriteSystem;
import de.vatterger.game.systems.graphics.BaseGUISystem;
import de.vatterger.game.systems.graphics.CullingSlaveSystem;
import de.vatterger.game.systems.graphics.CullingSystem;
import de.vatterger.game.systems.graphics.GraphicalProfilerSystem;
import de.vatterger.game.systems.graphics.ParentSystem;
import de.vatterger.game.systems.graphics.SpriteRenderSystem;
import de.vatterger.game.systems.graphics.TerrainColliderSystem;
import de.vatterger.game.systems.graphics.TerrainRenderSystem;
import de.vatterger.game.systems.graphics.TracerHitSystem;

public class GameScreen implements Screen {

	private Profiler				profiler			= null;

	private World					world				= null;

	private Camera					camera				= null;
	private Viewport				viewport			= null;
	private SpriteBatch				spriteBatch			= null;
	private RTSCameraController2D	camController		= null;
	private InputMultiplexer		inputMultiplexer	= null;

	private Stage					stage				= null;
	private Skin					skin				= null;
	
	public GameScreen() {

		setupInputMultiplexer();
		setupProfiler();
		setupCamera();
		setupSpriteBatch();
		setupStage();
		
		buildECSWorld();
		
		spawnUnits();
	}

	private void setupInputMultiplexer() {
		inputMultiplexer = new InputMultiplexer();
	}
	
	private void setupProfiler() {

		profiler = new Profiler("loop");
		
		GraphicalProfilerSystem.setCombinedProfiler(profiler);
	}

	private void setupCamera() {
		
		camera = new OrthographicCamera();
		
		camera.near	= 0f;
		camera.far	= 2048f;
		
		viewport = new ScalingViewport(Scaling.fit, Metrics.ww , Metrics.hw, camera);
		
		camController = new RTSCameraController2D(viewport, this);
		
		inputMultiplexer.addProcessor(camController);
	}
	
	private void setupSpriteBatch() {
		
		spriteBatch = new SpriteBatch(2048);
	}
	
	private void buildECSWorld() {
		
		WorldConfiguration config = new WorldConfiguration();
		
		config.register("camera", camera);
		config.register("stage", stage);
		config.register("skin", skin);
		config.register("input", inputMultiplexer);
		
		config.setSystem(new TimeSystem());
		
		config.setSystem(new CreateTestEntitySystem());
		
		config.setSystem(new AssignRandomPathsSystem());
		
		config.setSystem(new RemoveEntitySystem());
		
		config.setSystem(new RemoveTimedSystem());
		config.setSystem(new FadeSpriteSystem());
		
		config.setSystem(new AnimatedSpriteSystem());
		
		config.setSystem(new MoveByVelocitySystem());
		config.setSystem(new MoveAlongPathSystem());
		
		config.setSystem(new TracerHitSystem());
		
		config.setSystem(new CullingSystem());
		config.setSystem(new CullingSlaveSystem());
		
		config.setSystem(new ParentSystem());
		
		config.setSystem(new TerrainColliderSystem());
		
		config.setSystem(new MaintainCollisionMapSystem());
		//config.setSystem(new CollisionRadiusShapeRenderSystem(camera));
		//config.setSystem(new PathTestCalcAndRenderSystem(camera));
		
		config.setSystem(new TerrainRenderSystem());
		config.setSystem(new SpriteRenderSystem());
		
		config.setSystem(new BaseGUISystem());
		
		config.setSystem(new GraphicalProfilerSystem());
		
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
				{0,1,0,1,0,1,0},
				{1,0,1,0,1,0,1},
				{0,1,0,1,0,1,0},
				{1,0,0,0,1,1,1},
				{0,1,0,1,0,1,0},
				{1,0,1,0,1,0,1},
				{0,1,0,1,0,1,0},
		};
		
		for (int i = 0; i < 200; i++) {
			UnitHandlerJSON.createTank("pz6h", new Vector3(MathUtils.random(0f, 1500f), MathUtils.random(0f, 1500f), 0f), world);
		}
		
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				UnitHandlerJSON.createTerrainTile(m,new Vector3(150f*i, 150f*j, 0f), world);
			}
		}
		
		for (int i = 0; i < 5; i++) {
			UnitHandlerJSON.createStaticObject("house01", new Vector3(MathUtils.random(0f, 300f), MathUtils.random(0f, 500f), 0f), world);
		}
		
		for (int i = 0; i < 0; i++) {
			UnitHandlerJSON.createStaticObject("tree01", new Vector3(MathUtils.random(0f, 500f), MathUtils.random(0f, 500f), 0f), world);
		}
		
		for (int i = 0; i < 0; i++) {
			UnitHandlerJSON.createTank("m4a1", new Vector3(MathUtils.random(0f, 500f), MathUtils.random(0f, 500f), 0f), world);
			UnitHandlerJSON.createTank("pz6h", new Vector3(MathUtils.random(0f, 500f), MathUtils.random(0f, 500f), 0f), world);
		}
		
		for (int i = 0; i < 0; i++) {
			UnitHandlerJSON.createInfatry("soldier", new Vector3(MathUtils.random(0f, 500f), MathUtils.random(0f, 500f), 0f), world);
		}
	}
	
	
	private void setupStage() {
		
		skin = new Skin(Gdx.files.internal("assets/visui/assets/uiskin.json"));

		stage = new Stage();
		
		stage.setDebugAll(false);
		
		inputMultiplexer.addProcessor(stage);
	}

	@Override
	public void render(float delta) {
		
		//Crashes / Causes slow-downs on Ubuntu 18.04 x64!
		//Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()) + " - " + (int)((1f/Gdx.graphics.getRawDeltaTime()) + 0.5f)
		//		+ " - " + profiler.getTimeElapsed());
		
		profiler.start();
		
		camController.update();
		
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		world.setDelta(delta);
		world.process();
		
		stage.act(delta);
		stage.draw();

		profiler.stop();
		
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
	}

	@Override
	public void show() {
		
		Gdx.input.setInputProcessor(inputMultiplexer);
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
