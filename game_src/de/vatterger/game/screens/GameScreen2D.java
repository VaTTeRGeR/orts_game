
package de.vatterger.game.screens;

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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.vatterger.engine.camera.RTSCameraController2D;
import de.vatterger.engine.handler.asset.AssetPathFinder;
import de.vatterger.engine.handler.asset.AssetPathFinder.AssetPath;
import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.handler.unit.UnitHandler;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.systems.gameplay.CreateEntitySystem;
import de.vatterger.game.systems.gameplay.RemoveEntitySystem;
import de.vatterger.game.systems.gameplay.RotateEntitySystem;
import de.vatterger.game.systems.graphics.CullingSystem;
import de.vatterger.game.systems.graphics.FrameTimeDebugRenderSystem;
import de.vatterger.game.systems.graphics.ParentSystem;
import de.vatterger.game.systems.graphics.SpriteRenderSystem;

public class GameScreen2D implements Screen {

	World					world;
	Profiler				profiler;

	Camera					camera;
	Viewport				viewport;
	SpriteBatch				spriteBatch;
	RTSCameraController2D	camController;

	public GameScreen2D() {
		setupCamera();
		setupSpriteBatch();
		setupWorld();
		setupSprites();
		
		spawnUnits();
	}

	private void setupSpriteBatch() {
		spriteBatch = new SpriteBatch(4096);
	}
	
	private void setupCamera() {
		camera = new OrthographicCamera();
		viewport = new ScalingViewport(Scaling.fit, Metrics.ww , Metrics.hw, camera);
		camController = new RTSCameraController2D(viewport, this);
		Gdx.input.setInputProcessor(new InputMultiplexer(camController));
	}

	private void setupWorld() {
		WorldConfiguration config = new WorldConfiguration();
		
		config.setSystem(new RotateEntitySystem());
		config.setSystem(new CreateEntitySystem(camera));
		config.setSystem(new RemoveEntitySystem(camera));
		config.setSystem(new CullingSystem(camera));
		config.setSystem(new ParentSystem());
		config.setSystem(new SpriteRenderSystem(camera));
		config.setSystem(new FrameTimeDebugRenderSystem(profiler = new Profiler("loop")));

		world = new World(config);

		UnitHandler.setWorld(world);
		
	}

	private void setupSprites() {
		AtlasHandler.initialize();
		for (AssetPath path : AssetPathFinder.searchForAssets(".u", "data/tank")) {
			AtlasHandler.registerTankSprites(path.name);
		}

		for (AssetPath path : AssetPathFinder.searchForAssets(".u", "data/infantry")) {
			AtlasHandler.registerInfantrySprites(path.name);
		}

		for (AssetPath path : AssetPathFinder.searchForAssets(".u", "data/misc")) {
			AtlasHandler.registerMiscSprites(path.name);
		}

		for (AssetPath path : AssetPathFinder.searchForAssets(".u", "data/fx")) {
			AtlasHandler.registerMiscSprites(path.name);
		}
	}

	private void spawnUnits() {
		for (int x = 0; x < 1000; x+=10) {
			for (int y = 0; y < 1000; y+=10) {
				UnitHandler.createGroundTile("tile", new Vector3(x, y, 0f));
			}
		}
		
		UnitHandler.createInfatry("soldier", new Vector3(1f, 2f, 0f));
		
		for (int i = 0; i < 5; i++) {
			UnitHandler.createInfatry("soldier", new Vector3(2f*i-4f, -4f, 0f));
		}

		for (int i = 0; i < 5; i++) {
			UnitHandler.createInfatry("soldier", new Vector3(2f*i-4.25f, -3f, 0f));
		}

		UnitHandler.createTank("pz1b", new Vector3(0f, 0f, 0f));
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
		camController.resize(width, height);
	}

	@Override
	public void show() {
		System.out.println("SHOW");
	}

	@Override
	public void pause() {
		System.out.println("PAUSE");
	}

	@Override
	public void resume() {
		System.out.println("RESUME");
	}

	@Override
	public void hide() {
		System.out.println("HIDE");
	}

	@Override
	public void dispose() {
		AtlasHandler.dispose();
		spriteBatch.dispose();
	}
}
