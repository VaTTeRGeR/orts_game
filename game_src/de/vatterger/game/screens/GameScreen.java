package de.vatterger.game.screens;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;

import de.vatterger.engine.camera.RTSCameraController;
import de.vatterger.engine.handler.asset.ModelHandler;

public class GameScreen implements Screen {

	AssetManager manager;
	World world;
	Camera camera;
	RTSCameraController cameraController;
	Environment environment;
	ImmediateModeRenderer20 immediateRender;
	
	public GameScreen() {
		ModelHandler.loadModels(manager = new AssetManager());
		
		immediateRender = new ImmediateModeRenderer20(false, true, 0);
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE));
		
		camera = new PerspectiveCamera();
		camera.position.set(0f, 0f, 1.8f);
		camera.lookAt(0f, 10f, 1.8f);
		camera.near = 1f;
		camera.far = 1<<16;
		camera.update();

		cameraController = new RTSCameraController(camera);
		cameraController.setAcceleration(75f);
		cameraController.setMaxVelocity(300f/3.6f);
		cameraController.setDegreesPerPixel(0.25f);
		cameraController.setHeightRestriction(8f, 256f);
		cameraController.setPitchAngleRestriction(30f, 90f);
		
		WorldConfiguration config = new WorldConfiguration();
		world = new World(config);

		Gdx.input.setInputProcessor(new InputMultiplexer(cameraController));
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);

		cameraController.update(Gdx.graphics.getDeltaTime());

		world.setDelta(delta);
		world.process();
		
		immediateRender.begin(camera.combined, GL20.GL_LINES);
		immediateRender.color(Color.RED);
		immediateRender.vertex(0, 0, 0);
		immediateRender.color(Color.RED);
		immediateRender.vertex(10, 0, 0);
		immediateRender.end();
		immediateRender.begin(camera.combined, GL20.GL_LINES);
		immediateRender.color(Color.GREEN);
		immediateRender.vertex(0, 0, 0);
		immediateRender.color(Color.GREEN);
		immediateRender.vertex(0,10, 0);
		immediateRender.end();
		immediateRender.begin(camera.combined, GL20.GL_LINES);
		immediateRender.color(Color.BLUE);
		immediateRender.vertex(0, 0, 0);
		immediateRender.color(Color.BLUE);
		immediateRender.vertex(0, 0, 10);
		immediateRender.end();

		if(Gdx.input.isKeyPressed(Keys.ESCAPE))
			Gdx.app.exit();
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = Gdx.graphics.getWidth();
		camera.viewportHeight = Gdx.graphics.getHeight();
		camera.update();
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
		immediateRender.dispose();
		manager.dispose();
		world.dispose();
	}
}
