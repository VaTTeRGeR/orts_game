
package de.vatterger.game.screen;

import java.util.ArrayList;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.link.EntityLinkManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;

import de.vatterger.engine.camera.RTSCameraController2D;
import de.vatterger.engine.handler.unit.UnitBuilder;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.screen.manager.ScreenManager;
import de.vatterger.game.systems.gameplay.AssignStaticObjectSystem;
import de.vatterger.game.systems.gameplay.CreateTestEntitySystem;
import de.vatterger.game.systems.gameplay.DynamicObjectMapSystem;
import de.vatterger.game.systems.gameplay.FadeSpriteSystem;
import de.vatterger.game.systems.gameplay.MoveAlongPathSystem;
import de.vatterger.game.systems.gameplay.MoveByVelocitySystem;
import de.vatterger.game.systems.gameplay.RemoveTimedSystem;
import de.vatterger.game.systems.gameplay.StaticObjectMapSystem;
import de.vatterger.game.systems.gameplay.TimeSystem;
import de.vatterger.game.systems.graphics.AnimatedSpriteSystem;
import de.vatterger.game.systems.graphics.BaseGUISystem;
import de.vatterger.game.systems.graphics.CollisionFieldRenderSystem;
import de.vatterger.game.systems.graphics.CullingSlaveSystem;
import de.vatterger.game.systems.graphics.CullingSystem;
import de.vatterger.game.systems.graphics.GraphicalProfilerSystem;
import de.vatterger.game.systems.graphics.GridMapCullingSystem;
import de.vatterger.game.systems.graphics.InitializeCullingSystem;
import de.vatterger.game.systems.graphics.ParentSystem;
import de.vatterger.game.systems.graphics.SpriteRenderSystem;
import de.vatterger.game.systems.graphics.TerrainRenderSystemPrototype;
import de.vatterger.game.systems.graphics.TracerHitSystem;
import de.vatterger.game.systems.graphics.TurretRotateToMouseSystem;

public class GameScreen implements Screen {

	private Profiler profiler = null;

	private World world = null;

	private Camera camera = null;
	private Viewport viewport = null;
	private RTSCameraController2D camController = null;
	private InputMultiplexer inputMultiplexer = null;

	private Stage stage = null;
	private Skin skin = null;

	public GameScreen () {

		// TODO: Prototype Local Multiplayer with Android App to control units!!! => WARGAMING!
		
		setupInputMultiplexer();
		setupProfiler();
		setupCamera();
		setupStage();
		setupECSWorld();

		spawnUnits();
	}

	private void setupInputMultiplexer () {
		inputMultiplexer = new InputMultiplexer();
	}

	private void setupProfiler () {

		profiler = new Profiler("loop");

		GraphicalProfilerSystem.setCombinedProfiler(profiler);
	}

	private void setupCamera () {

		camera = new OrthographicCamera();

		camera.near	= -1f;
		camera.far	=  1f;

		viewport = new ScalingViewport(Scaling.fit, Metrics.ww, Metrics.hw, camera);

		camController = new RTSCameraController2D(viewport, this);

		inputMultiplexer.addProcessor(camController);
	}

	private void setupStage () {

		if (!VisUI.isLoaded()) {
			VisUI.load();
		}

		skin = VisUI.getSkin();

		Batch batch = new SpriteBatch(64);

		stage = new Stage(new ScalingViewport(Scaling.stretch, Metrics.wv, Metrics.hv), batch);

		stage.setDebugAll(false);

		inputMultiplexer.addProcessor(stage);
	}

	private void setupECSWorld () {

		WorldConfiguration config = new WorldConfiguration();

		config.register("viewport", viewport);
		config.register("camera", camera);
		config.register("stage", stage);
		config.register("skin", skin);
		config.register("inputMultiplexer", inputMultiplexer);

		config.setAlwaysDelayComponentRemoval(true);

		ArrayList<BaseSystem> configSystems = createSystems();

		for (BaseSystem system : configSystems) {
			config.setSystem(system);
		}

		world = new World(config);
	}

	private ArrayList<BaseSystem> createSystems () {

		ArrayList<BaseSystem> configSystems = new ArrayList<>(64);

		configSystems.add(new EntityLinkManager()); // Required by artemis-odb

		configSystems.add(new TimeSystem());

		// configSystems.add(new MusicSystem());

		configSystems.add(new CreateTestEntitySystem());
		
		// configSystems.add(new SmokePuffByVelocitySystem());

		// configSystems.add(new PathFindingSystem());

		// configSystems.add(new RemoveEntitySystem());

		configSystems.add(new TurretRotateToMouseSystem());
		
		configSystems.add(new RemoveTimedSystem());
		configSystems.add(new FadeSpriteSystem());

		configSystems.add(new AnimatedSpriteSystem());

		configSystems.add(new MoveByVelocitySystem());
		configSystems.add(new MoveAlongPathSystem());

		configSystems.add(new TracerHitSystem());

		configSystems.add(new ParentSystem());

		configSystems.add(new AssignStaticObjectSystem());
		configSystems.add(new DynamicObjectMapSystem());
		configSystems.add(new StaticObjectMapSystem());

		configSystems.add(new InitializeCullingSystem());
		configSystems.add(new GridMapCullingSystem());
		configSystems.add(new CullingSystem());
		configSystems.add(new CullingSlaveSystem());

		configSystems.add(new TerrainRenderSystemPrototype());

		configSystems.add(new CollisionFieldRenderSystem());

		configSystems.add(new SpriteRenderSystem());

		configSystems.add(new BaseGUISystem());

		configSystems.add(new GraphicalProfilerSystem());

		return configSystems;
	}

	private void spawnUnits () {

		int abc = new UnitBuilder("opel-blitz-a").spawnUnit(new Vector3(10, 10, 0), world);
		world.edit(abc).add(new AbsoluteRotation(135));
		
		int size = 2000;
		float sizef = (float)size;

		// 
		/*for (int x = 0; x < size; x += 20) {
			for (int y = 0; y < size; y += 20) {
				if(MathUtils.randomBoolean(0.15f)) {
					int entityId = UnitHandlerJSON.createTank("opel-blitz-a", new Vector3(x, y, 0), world);
					world.edit(entityId).add(new AbsoluteRotation(MathUtils.random(360f)));
				}
			}
		}*/

		for (int x = 0; x < size; x += 5) {
			for (int y = 0; y < size; y += 5) {
				
				int rand = MathUtils.random(9);
				
				if(rand == 0) {
					new UnitBuilder("pine-dense").spawnUnit(new Vector3(MathUtils.random((int)sizef), MathUtils.random((int)sizef), 0), world);
				}
				
				if(rand == 1) {
					new UnitBuilder("pine-stump").spawnUnit(new Vector3(MathUtils.random((int)sizef), MathUtils.random((int)sizef), 0), world);
				}

				if(rand == 2) {
					new UnitBuilder("pine-bald").spawnUnit(new Vector3(MathUtils.random((int)sizef), MathUtils.random((int)sizef), 0), world);
				}
			}
		}

		/*for (int i = 0; i < 1000; i++) {
			UnitHandlerJSON.createInfatry("soldier", new Vector3(MathUtils.random(sizef), MathUtils.random(sizef), 0f), world);
		}*/

		// Rail from lower left to upper right corner of the map.
		/*Vector3 railCurrentPosition = new Vector3();
		float railCurrentRotation = 315f;

		for (int i = 0; i < Math.sqrt(2f) * (size - 1f) / ((5.00f + 4.75f + 4.75f) / 3f); i++) {

			int entityId = UnitHandlerJSON.createStaticObject("rail_straight_long", railCurrentPosition, SpriteLayer.GROUND1, world);
			world.edit(entityId).add(new AbsoluteRotation(railCurrentRotation));

			float rotAdd = 22.5f * MathUtils.random(-1, 1);

			if (rotAdd == 0f) {
				railCurrentPosition.add(new Vector3(0f, 5.00f, 0f).rotate(Vector3.Z, railCurrentRotation));
			} else {
				railCurrentPosition.add(new Vector3(0f, 4.75f, 0f).rotate(Vector3.Z, railCurrentRotation));
			}

			railCurrentRotation = railCurrentRotation + rotAdd;
			railCurrentRotation = MathUtils.clamp(railCurrentRotation, 270f, 360f);
		}*/

	}

	@Override
	public void render (float delta) {

		// System.out.println("WW: " + viewport.getWorldWidth() + ", WH: " + viewport.getWorldHeight());
		// System.out.println("CX: " + camera.position.x + ", CY: " + camera.position.y);

		// Changing the title often causes Crashes / Causes slow-downs on Ubuntu 18.04 x64!
		// Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()) + " - " +
		// (int)((1f/Gdx.graphics.getRawDeltaTime()) + 0.5f)
		// + " - " + profiler.getTimeElapsed());

		// Minimum frametime of 50ms.
		delta = Math.min(Gdx.graphics.getDeltaTime(), 1f / 20f);

		profiler.start();

		camController.update(delta);

		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		world.setDelta(delta);
		world.process();

		stage.act(delta);
		stage.draw();

		profiler.stop();

		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			ScreenManager.pushScreen(ScreenManager.SETTINGS);
		}

		if (Gdx.input.isKeyJustPressed(Keys.F1) && Gdx.graphics.supportsDisplayModeChange()) {
			if (Gdx.graphics.isFullscreen())
				Gdx.graphics.setWindowedMode(640, 480);
			else
				Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		}
	}

	@Override
	public void resize (int width, int height) {

		Metrics.wv = width;
		Metrics.hv = height;

		Metrics.ww = Metrics.wv * camController.getZoom() / Metrics.ppm;
		Metrics.hw = Metrics.hv * camController.getZoom() / Metrics.ppm;

		viewport.setWorldSize(Metrics.ww, Metrics.hw);
		viewport.update(Metrics.wv, Metrics.hv, true);

		stage.getViewport().setWorldSize(Metrics.wv, Metrics.hv);
		stage.getViewport().update(Metrics.wv, Metrics.hv, true);
	}

	@Override
	public void show () {
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	@Override
	public void dispose () {
		stage.dispose();
	}

	@Override
	public void hide () {
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}
}
