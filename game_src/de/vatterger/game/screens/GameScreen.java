
package de.vatterger.game.screens;

import java.util.concurrent.TimeUnit;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.camera.RTSCameraController;
import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.util.GameUtil;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.ModelID;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;
import de.vatterger.game.components.gameobject.ShadowedModel;
import de.vatterger.game.components.gameobject.StaticModel;
import de.vatterger.game.components.gameobject.Transparent;
import de.vatterger.game.systems.gameplay.RemoveEntitySystem;
import de.vatterger.game.systems.graphics.CullingSystem;
import de.vatterger.game.systems.graphics.FrameTimeDebugRenderSystem;
import de.vatterger.game.systems.graphics.ModelDebugRenderSystem;
import de.vatterger.game.systems.graphics.ModelRenderSystem;
import de.vatterger.game.systems.graphics.ModelRenderTransparentSystem;
import de.vatterger.game.systems.graphics.ModelShadowMapSystem;

public class GameScreen implements Screen {

	private World					world;
	private Camera					camera;
	private Environment				environment;
	private RTSCameraController		cameraController;

	public GameScreen() {
		ModelHandler.loadModels();
		
		setupEnvironment();
		
		setupCamera();
		
		WorldConfiguration config = new WorldConfiguration();
		
		config.setSystem(new RemoveEntitySystem(camera));

		config.setSystem(new CullingSystem(camera));
		
		config.setSystem(new ModelShadowMapSystem(camera, environment));
		
		config.setSystem(new ModelRenderSystem(camera, environment));

		config.setSystem(new ModelRenderTransparentSystem(camera, environment));

		config.setSystem(new ModelDebugRenderSystem(camera));
		
		config.setSystem(new FrameTimeDebugRenderSystem(profiler));

		world = new World(config);
		
		Gdx.input.setInputProcessor(new InputMultiplexer(cameraController));
	}

	private void setupEnvironment() {
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE.cpy().mul(0.25f)));
		environment.add(new DirectionalLight().set(Color.WHITE.cpy().mul(0.75f), new Vector3(1f,1f,-1f)));
	}
	
	private void setupCamera() {
		camera = new PerspectiveCamera();
		camera.near = 4f;
		camera.far = 4096f;
		camera.update();

		cameraController = new RTSCameraController(camera);
		cameraController.setAcceleration(200f);
		cameraController.setMaxVelocity(150f);
		cameraController.setDegreesPerPixel(0.25f);
		cameraController.setHeightRestriction(8f, 256f);
		cameraController.setPitchAngleRestriction(45f, 65f);
		
		cameraController.setPosition(256f, 256f, 64f);
		cameraController.setDirection(1f, 1f);
	}

	Profiler profiler = new Profiler("render loop", TimeUnit.NANOSECONDS);
	
	@Override
	public void render(float delta) {
		profiler.start();
		
		Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()) + " - " + (int)((1f/Gdx.graphics.getDeltaTime()) + 0.5f));
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		cameraController.update(delta);

		if(Gdx.input.isButtonPressed(Buttons.LEFT)) {
			Vector3 iv = GameUtil.intersectMouseGroundPlane(camera, Gdx.input.getX(), Gdx.input.getY());

				for (int j = 0; j < 10; j++) {
				float angle = MathUtils.random(360f);
				float randomShift = 30f;
				world.edit(world.create())
				.add(new Position(iv.x+MathUtils.random(-randomShift,randomShift), iv.y+MathUtils.random(-randomShift,randomShift), iv.z))
				.add(new Rotation().set(new Quaternion(Vector3.Z, angle)))
				.add(new ModelID(ModelHandler.getModelId("tree01")))
				.add(new ShadowedModel())
				.add(new StaticModel())
				.add(new Transparent(true))
				.add(new CullDistance(64f));
			}
		}

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
		world.dispose();
		ModelHandler.dispose();
	}
}
