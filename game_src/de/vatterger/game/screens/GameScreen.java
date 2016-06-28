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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.camera.RTSCameraController;
import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.game.components.unit.Model;
import de.vatterger.game.components.unit.Position;
import de.vatterger.game.components.unit.Rotation;
import de.vatterger.game.systems.CoordinateArrowProcessor;
import de.vatterger.game.systems.ModelRenderSystem;
import de.vatterger.game.systems.ModelShadowMapSystem;

@SuppressWarnings("deprecation")
public class GameScreen implements Screen {

	AssetManager manager;
	World world;
	Camera camera;
	RTSCameraController cameraController;
	Environment environment;
	ImmediateModeRenderer20 immediateRenderer;
	DirectionalShadowLight shadowLight;
	
	public GameScreen() {
		ModelHandler.loadModels(manager = new AssetManager());
		
		immediateRenderer = new ImmediateModeRenderer20(false, true, 0);
		
		shadowLight = new DirectionalShadowLight(2048, 2048, 128, 128, 0, 512);
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE.cpy().mul(0.35f)));
		environment.add(new DirectionalLight().set(Color.WHITE.cpy().mul(0.75f), new Vector3(1f,1f,-1f)));
		environment.shadowMap = shadowLight;
		shadowLight.set(new Color(Color.BLACK), 1f, 1f, -1f);
		
		camera = new PerspectiveCamera();
		camera.near = 1f;
		camera.far = 1<<16;
		camera.update();

		cameraController = new RTSCameraController(camera);
		cameraController.setAcceleration(50f);
		cameraController.setMaxVelocity(300f/3.6f);
		cameraController.setDegreesPerPixel(0.25f);
		cameraController.setHeightRestriction(8f, 512f);
		cameraController.setPitchAngleRestriction(30f, 90f);
		
		cameraController.setPosition(0f, 0f, 64f);
		cameraController.setDirection(1f, 1f);
		
		WorldConfiguration config = new WorldConfiguration();
		config.setSystem(new ModelShadowMapSystem(shadowLight, camera));
		config.setSystem(new ModelRenderSystem(camera, environment));
		config.setSystem(new CoordinateArrowProcessor(immediateRenderer, camera));
		world = new World(config);
		
		world.edit(world.create()).
			add(new Position(0, 0, 0)).
			add(new Rotation(new Quaternion(Vector3.Z, 0f))).
			add(new Model(ModelHandler.getModelId("grw34")));
		
		world.edit(world.create()).
			add(new Position(0, 0, 0)).
			add(new Rotation(new Quaternion(Vector3.Z, 0f))).
			add(new Model(ModelHandler.getModelId("terrain")));
		
		world.edit(world.create()).
			add(new Position(10, 20, 0)).
			add(new Rotation(new Quaternion(Vector3.Z, 30f))).
			add(new Model(ModelHandler.getModelId("panzer_i_b")));
		
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
		shadowLight.dispose();
		immediateRenderer.dispose();
		manager.dispose();
		world.dispose();
	}
}
