
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
import com.badlogic.gdx.graphics.g2d.TextureArraySpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;

import de.vatterger.engine.camera.RTSCameraController2D;
import de.vatterger.engine.handler.unit.UnitHandlerJSON;
import de.vatterger.engine.util.GameUtil;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.SpriteLayer;
import de.vatterger.game.screen.manager.ScreenManager;
import de.vatterger.game.systems.gameplay.CreateTestEntitySystem;
import de.vatterger.game.systems.gameplay.FadeSpriteSystem;
import de.vatterger.game.systems.gameplay.MaintainCollisionMapSystem;
import de.vatterger.game.systems.gameplay.MoveAlongPathSystem;
import de.vatterger.game.systems.gameplay.MoveByVelocitySystem;
import de.vatterger.game.systems.gameplay.PathFindingSystem;
import de.vatterger.game.systems.gameplay.RemoveTimedSystem;
import de.vatterger.game.systems.gameplay.TerrainColliderSystem;
import de.vatterger.game.systems.gameplay.TimeSystem;
import de.vatterger.game.systems.graphics.AnimatedSpriteSystem;
import de.vatterger.game.systems.graphics.BaseGUISystem;
import de.vatterger.game.systems.graphics.CullingSlaveSystem;
import de.vatterger.game.systems.graphics.CullingSystem;
import de.vatterger.game.systems.graphics.GraphicalProfilerSystem;
import de.vatterger.game.systems.graphics.ParentSystem;
import de.vatterger.game.systems.graphics.PathTestCalcAndRenderSystem;
import de.vatterger.game.systems.graphics.SpriteRenderSystem;
import de.vatterger.game.systems.graphics.TerrainPaintSystem;
import de.vatterger.game.systems.graphics.TerrainRenderSystem;
import de.vatterger.game.systems.graphics.TerrainRenderSystemPrototype;
import de.vatterger.game.systems.graphics.TracerHitSystem;

public class GameScreen implements Screen {

	private Profiler				profiler			= null;
	
	private World					world				= null;
	
	private Camera					camera				= null;
	private Viewport				viewport			= null;
	private RTSCameraController2D	camController		= null;
	private InputMultiplexer		inputMultiplexer	= null;
	
	private Stage					stage				= null;
	private Skin					skin				= null;
	
	public GameScreen() {
		
		setupInputMultiplexer();
		setupProfiler();
		setupCamera();
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
	
	private void buildECSWorld() {
		
		WorldConfiguration config = new WorldConfiguration();
		
		config.register("camera", camera);
		config.register("stage", stage);
		config.register("skin", skin);
		config.register("input", inputMultiplexer);
		
		ArrayList<BaseSystem> configSystems = createSystems();
		
		for (BaseSystem system : configSystems) {
			config.setSystem(system);
		}
		
		world = new World(config);
	}
	
	private ArrayList<BaseSystem> createSystems() {
		
		ArrayList<BaseSystem> configSystems = new ArrayList<>(64);
		
		configSystems.add(new EntityLinkManager());
		
		configSystems.add(new TimeSystem());
		
		//configSystems.add(new MusicSystem());
		
		configSystems.add(new CreateTestEntitySystem());
		//configSystems.add(new SmokePuffByVelocitySystem());
		
		//configSystems.add(new PathFindingSystem());
		
		//configSystems.add(new RemoveEntitySystem());
		
		configSystems.add(new RemoveTimedSystem());
		configSystems.add(new FadeSpriteSystem());
		
		configSystems.add(new AnimatedSpriteSystem());
		
		//configSystems.add(new MoveByVelocitySystem());
		//configSystems.add(new MoveAlongPathSystem());
		
		configSystems.add(new TracerHitSystem());
		
		configSystems.add(new CullingSystem());
		configSystems.add(new CullingSlaveSystem());
		
		configSystems.add(new ParentSystem());
		
		configSystems.add(new TerrainColliderSystem());
		
		configSystems.add(new MaintainCollisionMapSystem());
		
		configSystems.add(new TerrainPaintSystem());
		
		//configSystems.add(new TerrainRenderSystem());
		//configSystems.add(new TerrainRenderSystemPrototype());
		
		configSystems.add(new SpriteRenderSystem());
		
		//configSystems.add(new CollisionRadiusShapeRenderSystem());
		
		//configSystems.add(new PathTestCalcAndRenderSystem(camera));
		
		//configSystems.add(new TerrainDebugRenderSystem());
		
		configSystems.add(new BaseGUISystem());
		
		configSystems.add(new GraphicalProfilerSystem());

		return configSystems;
	}

	private void spawnUnits() {
		
		float m[][] = new float[21][21];
		
		/*float m[][] = {
				{0,1,0,1,1,1,0},
				{1,1,1,1,1,1,1},
				{0,1,1,1,1,1,0},
				{1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1},
				{0,1,0,1,1,1,0},
		};*/
		
		int size = 1500;
		float sizef = (float)size;
		
		float cellSize = 10f;
		float tileSizeX = cellSize * (m[0].length - 1);
		float tileSizeY = cellSize * (m.length - 1);
		
		for (int a = 0; a < size/tileSizeX; a++) {
			
			for (int b = 0; b < size/tileSizeY; b++) {
				
				for (int i = 0; i < m.length; i++) {
					
					for (int j = 0; j < m[i].length; j++) {
						
						if(i == 0 || j == 0 || i == m.length - 1 || j == m[i].length - 1) {
							m[i][j] = 1f;
						} else {
							m[i][j] = MathUtils.random(1f);
							//m[i][j] = Math.min(MathUtils.random(1f) + MathUtils.random(1f), 1f);
							//m[i][j] = MathUtils.clamp((i - 1)/(float)(m.length - 2), 0f, 1f);
						}
					}
				}
				
				UnitHandlerJSON.createTerrainTile(m, cellSize, new Vector3(tileSizeX*a, tileSizeY*b, 0f), world);
			}
		}
		
		for (int i = 0; i < 100; i++) {
			UnitHandlerJSON.createTank("m4a1", new Vector3(MathUtils.random(sizef), MathUtils.random(sizef), 0f), world);
		}
		
		for (int i = 0; i < 1000; i++) {
			int entityId = UnitHandlerJSON.createStaticObject("mg_bunker", new Vector3(MathUtils.random(sizef), MathUtils.random(sizef), 0f), world);
			world.edit(entityId).add(new AbsoluteRotation(MathUtils.random(360f)));
		}
		
		for (int i = 0; i < 100; i++) {
			UnitHandlerJSON.createStaticObject("tree03", new Vector3(MathUtils.random(sizef), MathUtils.random(sizef), 0f), world);
		}
		
		for (int i = 0; i < 5000; i++) {
			UnitHandlerJSON.createStaticObject("tree01", new Vector3(MathUtils.random(sizef), MathUtils.random(sizef), 0f), world);
			UnitHandlerJSON.createStaticObject("tree02", new Vector3(MathUtils.random(sizef), MathUtils.random(sizef), 0f), world);
			UnitHandlerJSON.createStaticObject("tree04", new Vector3(MathUtils.random(sizef), MathUtils.random(sizef), 0f), world);
		}
		
		for (int i = 0; i < 200; i++) {
			UnitHandlerJSON.createInfatry("soldier", new Vector3(MathUtils.random(sizef), MathUtils.random(sizef), 0f), world);
		}
		
		Vector3 railCurrentPosition = new Vector3();
		float railCurrentRotation = 315f;
		
		for (int i = 0; i < Math.sqrt(2f) * (size - 1f) / ((5.00f + 4.75f + 4.75f) / 3f); i++) {
			
			int entityId = UnitHandlerJSON.createStaticObject("rail_straight_long", railCurrentPosition, SpriteLayer.GROUND1, world);
			world.edit(entityId).add(new AbsoluteRotation(railCurrentRotation));
			
			float rotAdd = 22.5f * MathUtils.random(-1, 1);
			
			if(rotAdd == 0f) {
				railCurrentPosition.add(new Vector3(0f, 5.00f, 0f).rotate(Vector3.Z, railCurrentRotation));
			} else {
				railCurrentPosition.add(new Vector3(0f, 4.75f, 0f).rotate(Vector3.Z, railCurrentRotation));
			}
			
			railCurrentRotation = railCurrentRotation + rotAdd;
			railCurrentRotation = MathUtils.clamp(railCurrentRotation, 270f, 360f);
		}
	}

	private void setupStage() {
		
		if(!VisUI.isLoaded()) {
			VisUI.load();
		}
		
		skin = VisUI.getSkin();
		
		stage = new Stage(new ScalingViewport(Scaling.stretch, Metrics.wv, Metrics.hv), new TextureArraySpriteBatch(128));
		
		stage.setDebugAll(false);
		
		inputMultiplexer.addProcessor(stage);
	}

	private static final int FRAME_AVERAGING_COUNT = 5;
	private FloatArray frameTimes = new FloatArray(true, FRAME_AVERAGING_COUNT);
	private float previousDelta = 1f/60f;

	private float calculateFrameTime(float delta) {

		
		if(frameTimes.size == frameTimes.items.length) {
			frameTimes.removeIndex(0);
		}
		
		frameTimes.add(delta);
		
		final float maxVariation = 2.5f /*ms*/ / 1000f;
		
		float delta_sum = 0;
		float delta_size = 0;
		
		for (float delta_item : frameTimes.items) {
			
			if(Math.abs(delta_item - previousDelta) < maxVariation) {
				
				delta_sum += delta_item;
				
				delta_size ++;
			}
		}
		
		if(delta_size > 0) {
			delta = delta_sum / delta_size;
		}
		
		//System.out.println(delta * 1000f);
		
		return previousDelta = MathUtils.clamp(delta, 1f/120f, 1f/10f);
	}
	
	@Override
	public void render(float delta) {
		
		//System.out.println("WW: " + viewport.getWorldWidth() + ", WH: " + viewport.getWorldHeight());
		//System.out.println("CX: " + camera.position.x + ", CY: " + camera.position.y);
		
		//Crashes / Causes slow-downs on Ubuntu 18.04 x64!
		//Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()) + " - " + (int)((1f/Gdx.graphics.getRawDeltaTime()) + 0.5f)
		//		+ " - " + profiler.getTimeElapsed());
		
		delta = calculateFrameTime(delta);
		
		profiler.start();
		
		camController.update(delta);
		
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
		
		Metrics.ww = Metrics.wv * camController.getZoom() / Metrics.ppm;
		Metrics.hw = Metrics.hv * camController.getZoom() / Metrics.ppm;
		
		viewport.setWorldSize(Metrics.ww , Metrics.hw);
		viewport.update(Metrics.wv, Metrics.hv, true);
		
		stage.getViewport().setWorldSize(Metrics.wv, Metrics.hv);
		stage.getViewport().update(Metrics.wv, Metrics.hv, true);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	@Override
	public void dispose() {	
		stage.dispose();
	}

	@Override
	public void hide() {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}
}
