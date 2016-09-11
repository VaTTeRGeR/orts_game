
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
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.vatterger.engine.camera.RTSCameraController2D;
import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.systems.graphics.FrameTimeDebugRenderSystem;

public class GameScreen2D implements Screen {

	World					world;
	Profiler				profiler;

	Camera					camera;
	Viewport				viewport;
	SpriteBatch				spriteBatch;
	RTSCameraController2D	camController;
	
	float angle = 0f;
	Vector3 offset = new Vector3(0.1f, -0.1f, 1.38f);
	Vector3 temp = new Vector3();
	
	public GameScreen2D() {
		setupCamera();
		setupUnits();
		setupWorld();
		spriteBatch = new SpriteBatch(5460);
	}

	private void setupWorld() {
		WorldConfiguration config = new WorldConfiguration();
		
		config.setSystem(new FrameTimeDebugRenderSystem(profiler = new Profiler("MainLoop")));

		world = new World(config);
	}

	private void setupCamera() {
		camera = new OrthographicCamera();
		viewport = new ScalingViewport(Scaling.fit, Metrics.ww , Metrics.hw, camera);
		camController = new RTSCameraController2D(viewport, this);
		Gdx.input.setInputProcessor(new InputMultiplexer(camController));
	}
	
	private void setupUnits() {
		AtlasHandler.initialize(Metrics.sssm);
		AtlasHandler.registerTank("tank", 1);
		AtlasHandler.registerTank("pz1b", 1);
		AtlasHandler.registerSoldier("soldier");
		AtlasHandler.registerMisc("tile");
	}
	
	@Override
	public void render(float delta) {
		profiler.start();
		
		camController.update();
		
		Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()) + " - " + (int)((1f/Gdx.graphics.getDeltaTime()) + 0.5f));
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		angle += 45 * delta;
		if(angle >= 360f)
			angle -= 360f;
		
		Sprite sprite0 = AtlasHandler.getSharedSpriteFromId(AtlasHandler.getIdFromName("tile"));
		Sprite sprite1 = AtlasHandler.getSharedSpriteFromId(AtlasHandler.getIdFromName("pz1b_h"), Math2D.angleToIndex(angle));
		Sprite sprite2 = AtlasHandler.getSharedSpriteFromId(AtlasHandler.getIdFromName("pz1b_t0"), Math2D.angleToIndex(angle+45f));
		Sprite sprite3 = AtlasHandler.getSharedSpriteFromId(AtlasHandler.getIdFromName("soldier_p0"), Math2D.angleToIndex(angle));
		
		temp.set(offset).rotate(Vector3.Z, Math2D.roundAngleEight(angle));
		
		sprite1.setPosition(-sprite1.getWidth()/2f,-sprite1.getHeight()/2f);
		sprite2.setPosition(-sprite2.getWidth()/2f + temp.x, temp.y*Metrics.ymod+temp.z*Metrics.ymod-sprite2.getHeight()/2f);
		sprite3.setPosition(-sprite3.getWidth()/2f + 5f, -sprite3.getHeight()/2f);

		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.enableBlending();
		spriteBatch.begin();
		
		for (int i = 800; i >= 0; i -= 10) {
			for (int j = 600; j >= 0; j -= 10) {
				sprite0.setPosition(i-sprite2.getWidth()/2f,j*Metrics.ymod-sprite2.getHeight()/2f);
				sprite0.draw(spriteBatch);
			}
		}
		
		sprite1.draw(spriteBatch);
		sprite2.draw(spriteBatch);
		sprite3.draw(spriteBatch);
		
		spriteBatch.end();
		
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.F1) && Gdx.graphics.supportsDisplayModeChange()) {
			if(Gdx.graphics.isFullscreen())
				Gdx.graphics.setWindowedMode(640, 480);
			else
				Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		}
		world.setDelta(delta);
		world.process();
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
