
package de.vatterger.game.screens;

import java.util.Arrays;
import java.util.Comparator;

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
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
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
import de.vatterger.game.systems.graphics.FrameTimeDebugRenderSystem;
import de.vatterger.game.systems.graphics.ModelDebugRenderSystem;
import de.vatterger.game.systems.graphics.ModelDynamicCacheRenderSystem;
import de.vatterger.game.systems.graphics.ModelDynamicCacheRenderTransparentSystem;
import de.vatterger.game.systems.graphics.ModelRenderSystem;
import de.vatterger.game.systems.graphics.ModelRenderTransparentSystem;
import de.vatterger.game.systems.graphics.ModelShadowMapSystem;

public class GameScreen implements Screen {

	private World world;

	private Camera camera;
	private Environment environment;
	private RTSCameraController cameraController;
	private ImmediateModeRenderer20 immediateRenderer;
	
	public GameScreen() {
		ModelHandler.searchAndLoadModels();
		
		immediateRenderer = new ImmediateModeRenderer20(false, true, 0);
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE.cpy().mul(0.25f)));
		environment.add(new DirectionalLight().set(Color.WHITE.cpy().mul(0.75f), new Vector3(1f,1f,-1f)));
		
		camera = new PerspectiveCamera();
		camera.near = 4f;
		camera.far = 4096f;
		camera.update();

		cameraController = new RTSCameraController(camera);
		cameraController.setAcceleration(200f);
		cameraController.setMaxVelocity(150f);
		cameraController.setDegreesPerPixel(0.25f);
		cameraController.setHeightRestriction(8f, 256f);
		cameraController.setPitchAngleRestriction(45f, 85f);
		
		cameraController.setPosition(256f, 256f, 64f);
		cameraController.setDirection(1f, 1f);
		
		WorldConfiguration config = new WorldConfiguration();
		
		config.setSystem(new ModelShadowMapSystem(camera, environment));
		
		config.setSystem(new ModelRenderSystem(camera, environment));
		config.setSystem(new ModelDynamicCacheRenderSystem(camera, environment));

		config.setSystem(new ModelRenderTransparentSystem(camera, environment));
		config.setSystem(new ModelDynamicCacheRenderTransparentSystem(camera, environment));

		config.setSystem(new ModelDebugRenderSystem(camera));
		config.setSystem(new FrameTimeDebugRenderSystem());

		world = new World(config);
		
		for (int i = 0; i < 100; i++) {
			float angle = MathUtils.random(360f);
			float turretAngle = -angle;
			world.edit(world.create())
			.add(new Position(MathUtils.random(10f*64f), MathUtils.random(10f*64f), 0))
			.add(new Rotation().set(new Quaternion(Vector3.Z, angle), new Quaternion(Vector3.Z, turretAngle)).set("a","aa"))
			.add(new ModelID(ModelHandler.getModelId("panzeri")))
			.add(new ShadowedModel())
			.add(new CullDistance(8f));
		}
	
		for (int i = 0; i < 100; i++) {
			world.edit(world.create())
			.add(new Position(MathUtils.random(10f*64f), MathUtils.random(10f*64f), 0f))
			.add(new Rotation(new Quaternion(Vector3.Z, MathUtils.random(360f))))
			.add(new ModelID(ModelHandler.getModelId("grw34")))
			.add(new ShadowedModel())
			.add(new CullDistance(8f));
		}
	
		int trees = 500;
		Vector3[] pos = new Vector3[trees];
		for (int i = 0; i < trees; i++) {
			pos[i] = new Vector3(MathUtils.random(64*10), MathUtils.random(64*10), 0f);
		}
		
		Arrays.sort(pos, new Comparator<Vector3>() {
			@Override
			public int compare(Vector3 o1, Vector3 o2) {
				if(o1.equals(o2)) {
					return 0;
				} else {
					return (int)(o1.x-o2.x);
				}
			}
		});
		
		for (int i = 0; i < trees; i++) {
			world.edit(world.create())
			.add(new Position(pos[i].x, pos[i].y, pos[i].z))
			.add(new Rotation(new Quaternion(Vector3.Z, MathUtils.random(360f))))
			.add(new ModelID(ModelHandler.getModelId("tree01")))
			.add(new ShadowedModel())
			.add(new StaticModel())
			.add(new CullDistance(64f))
			.add(new Transparent(true));
		}
	
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 11; j++) {
				world.edit(world.create())
				.add(new Position(i*64f, j*64f, 0f))
				.add(new Rotation(new Quaternion()))
				.add(new ModelID(ModelHandler.getModelId("terrain")))
				.add(new StaticModel())
				.add(new CullDistance(84f));
			}
		}
		
		Gdx.input.setInputProcessor(new InputMultiplexer(cameraController));
		Gdx.graphics.setVSync(true);
	}

	Profiler p = new Profiler("world.process");
	
	@Override
	public void render(float delta) {
		Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()) + " - " + (int)((1f/Gdx.graphics.getDeltaTime()) + 0.5f));

		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		cameraController.update(delta);

		if(Gdx.input.isButtonPressed(Buttons.LEFT)) {
			Vector3 pos = GameUtil.intersectMouseGroundPlane(camera, Gdx.input.getX(), Gdx.input.getY());

			for (int i = 0; i < 1; i++) {
				float angle = MathUtils.random(360f);
				float turretAngle = -angle;
				float randomShift = 4f;
				world.edit(world.create())
				.add(new Position(pos.x+MathUtils.random(-randomShift,randomShift), pos.y+MathUtils.random(-randomShift,randomShift), pos.z))
				.add(new Rotation().set(new Quaternion(Vector3.Z, angle), new Quaternion(Vector3.Z, turretAngle)).set("a","aa"))
				.add(new ModelID(ModelHandler.getModelId("tree01")))
				.add(new StaticModel())
				.add(new ShadowedModel())
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
		immediateRenderer.dispose();
		world.dispose();
		ModelHandler.dispose();
	}
}
