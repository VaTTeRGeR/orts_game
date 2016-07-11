
package de.vatterger.game.screens;

import java.util.concurrent.TimeUnit;

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
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.camera.RTSCameraController;
import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Model;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;
import de.vatterger.game.components.gameobject.ShadowedModel;
import de.vatterger.game.components.gameobject.StaticModel;
import de.vatterger.game.components.gameobject.Transparent;
import de.vatterger.game.systems.graphics.CoordinateArrowProcessor;
import de.vatterger.game.systems.graphics.DecalRenderSystem;
import de.vatterger.game.systems.graphics.ModelCacheRenderSystem;
import de.vatterger.game.systems.graphics.ModelCacheRenderTransparentSystem;
import de.vatterger.game.systems.graphics.ModelDebugRenderSystem;
import de.vatterger.game.systems.graphics.ModelRenderSystem;
import de.vatterger.game.systems.graphics.ModelRenderTransparentSystem;
import de.vatterger.game.systems.graphics.ModelShadowMapSystem;

public class GameScreen implements Screen {

	private World world;

	private Camera camera;
	private AssetManager manager;
	private Environment environment;
	private RTSCameraController cameraController;
	private ImmediateModeRenderer20 immediateRenderer;
	
	public GameScreen() {
		ModelHandler.loadModels(manager = new AssetManager());
		
		immediateRenderer = new ImmediateModeRenderer20(false, true, 0);
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE.cpy().mul(0.35f)));
		environment.add(new DirectionalLight().set(Color.WHITE.cpy().mul(0.75f), new Vector3(1f,1f,-1f)));
		
		camera = new PerspectiveCamera();
		camera.near = 4f;
		camera.far = 4096f;
		camera.update();

		cameraController = new RTSCameraController(camera);
		cameraController.setAcceleration(50f);
		cameraController.setMaxVelocity(300f/3.6f);
		cameraController.setDegreesPerPixel(0.25f);
		cameraController.setHeightRestriction(16f, 512f);
		cameraController.setPitchAngleRestriction(15f, 85f);
		
		cameraController.setPosition(256f, 256f, 64f);
		cameraController.setDirection(1f, 1f);
		
		WorldConfiguration config = new WorldConfiguration();
		
		config.setSystem(new ModelShadowMapSystem(camera, environment));
		
		config.setSystem(new ModelRenderSystem(camera, environment));
		config.setSystem(new ModelCacheRenderSystem(camera, environment));

		config.setSystem(new ModelRenderTransparentSystem(camera, environment));
		config.setSystem(new ModelCacheRenderTransparentSystem(camera, environment));

		config.setSystem(new CoordinateArrowProcessor(immediateRenderer, camera));
		config.setSystem(new ModelDebugRenderSystem(immediateRenderer, camera));

		config.setSystem(new DecalRenderSystem(camera, environment));
		
		world = new World(config);
		
		for (int i = 0; i < 500; i++) {
			world.edit(world.create())
			.add(new Position(MathUtils.random(25f*64f), MathUtils.random(25f*64f), 0f))
			.add(new Rotation(new Quaternion(Vector3.Z, (i*30)%360f)))
			.add(new Model(ModelHandler.getModelId("panzer_i_b")))
			.add(new ShadowedModel())
			.add(new CullDistance(8f));
		}
	
		for (int i = 0; i < 100; i++) {
			world.edit(world.create())
			.add(new Position(MathUtils.random(25f*64f), MathUtils.random(25f*64f), 0f))
			.add(new Rotation(new Quaternion(Vector3.Z, (i*30)%360f)))
			.add(new Model(ModelHandler.getModelId("grw34")))
			.add(new ShadowedModel())
			.add(new CullDistance(8f));
		}
	
		for (int i = 0; i < 500; i++) {
			world.edit(world.create())
			.add(new Position(MathUtils.random(50f*64f), MathUtils.random(25f*64f), 0f))
			.add(new Rotation(new Quaternion(Vector3.Z, (i*30)%360f)))
			.add(new Model(ModelHandler.getModelId("trees")))
			.add(new StaticModel())
			.add(new ShadowedModel())
			.add(new Transparent(true));
;
		}
	
		for (int i = 0; i < 25; i++) {
			for (int j = 0; j < 25; j++) {
				world.edit(world.create())
				.add(new Position(i*64f, j*64f, 0f))
				.add(new Rotation(new Quaternion(Vector3.Z, 0f)))
				.add(new Model(ModelHandler.getModelId("terrain")))
				.add(new StaticModel());
			}
		}
		
		Gdx.input.setInputProcessor(new InputMultiplexer(cameraController));
	}

	Profiler p = new Profiler("world.process", TimeUnit.MICROSECONDS);
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		cameraController.update(delta);

		p.start();
		
		world.setDelta(delta);
		world.process();

		p.log();

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
		immediateRenderer.dispose();
		manager.dispose();
		world.dispose();
	}
}
