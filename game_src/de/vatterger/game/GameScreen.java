package de.vatterger.game;

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

import de.vatterger.engine.camera.RTSCameraController;
import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.game.components.Model;
import de.vatterger.game.components.Position;
import de.vatterger.game.components.Rotation;
import de.vatterger.game.systems.UnitRenderSystem;

public class GameScreen implements Screen {

	AssetManager manager;
	World world;
	Camera camera;
	RTSCameraController cameraController;
	Environment environment;
	
	public GameScreen() {
		ModelHandler.loadModels(manager = new AssetManager());
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE));
		
		camera = new PerspectiveCamera();
		camera.position.set(0f, 0f, 1.8f);
		camera.lookAt(0f, 10f, 1.8f);
		camera.near = 1f;
		camera.far = 10000;
		camera.update();

		cameraController = new RTSCameraController(camera);
		cameraController.setAcceleration(50f);
		cameraController.setMaxVelocity(300f/3.6f);
		cameraController.setDegreesPerPixel(0.5f);
		cameraController.setHeightRestriction(12f, 224f);
		cameraController.setAngleRestriction(30f, 89f);
		
		WorldConfiguration config = new WorldConfiguration();
		config.setSystem(new UnitRenderSystem(camera,environment));
		world = new World(config);

		Gdx.input.setInputProcessor(new InputMultiplexer(cameraController));
		
		world.createEntity().edit().add(new Position(0, 0, 0)).add(new Model(ModelHandler.getModelId("panzer_i_b"))).add(new Rotation(0f));
		world.createEntity().edit().add(new Position(10, 0, 0)).add(new Model(ModelHandler.getModelId("panzer_i_b"))).add(new Rotation(45f));
		world.createEntity().edit().add(new Position(20, 0, 0)).add(new Model(ModelHandler.getModelId("panzer_i_b"))).add(new Rotation(90f));
		world.createEntity().edit().add(new Position(30, 0, 0)).add(new Model(ModelHandler.getModelId("panzer_i_b"))).add(new Rotation(135f));
		world.createEntity().edit().add(new Position(40, 0, 0)).add(new Model(ModelHandler.getModelId("panzer_i_b"))).add(new Rotation(180f));
	}
	
	@Override
	public void show() {
		
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);

		cameraController.update(Gdx.graphics.getDeltaTime());

		world.setDelta(delta);
		world.process();
		
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
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void dispose() {
		manager.dispose();
		world.dispose();
	}
}
