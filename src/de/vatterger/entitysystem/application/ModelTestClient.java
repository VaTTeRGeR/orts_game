package de.vatterger.entitysystem.application;

import static de.vatterger.entitysystem.application.GameConstants.XY_BOUNDS;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.MathUtils;

import de.vatterger.entitysystem.camera.RTSCameraController;
import de.vatterger.entitysystem.handler.asset.ModelHandler;
import de.vatterger.entitysystem.processors.client.DrawGLDebugProcessor;

public class ModelTestClient extends ApplicationAdapter implements InputProcessor {

	private World world;

	private int sw, sh;
	@SuppressWarnings("unused")
	private float vw, vh, ratio;

	private AssetManager assetManager;
	private ModelBatch modelBatch;
	private Camera camera3d;
	private RTSCameraController camera3dController;
	private Environment environment;
	
	@Override
	public void create() {
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, new Color(Color.WHITE)));
		
		camera3d = new PerspectiveCamera(90, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera3d.position.set(0f, 0f, 1.8f);
		camera3d.lookAt(0f, 10f, 1.8f);
		camera3d.near = 1f;
		camera3d.far = 8000;
		camera3d.update();

		camera3dController = new RTSCameraController(camera3d);
		camera3dController.setAcceleration(50f);
		camera3dController.setMaxVelocity(300f/3.6f);
		camera3dController.setDegreesPerPixel(0.5f);
		camera3dController.setHeightRestriction(4f, 224f);
		camera3dController.setAngleRestriction(15f, 89f);

		ModelHandler.loadModels(assetManager = new AssetManager());

		modelBatch = new ModelBatch();
		
		WorldConfiguration worldConfig = new WorldConfiguration();
		worldConfig.setSystem(new DrawGLDebugProcessor(camera3d));

		world = new World(worldConfig);

		Gdx.input.setInputProcessor(new InputMultiplexer(this, camera3dController));
	}

	private void act(float delta) {
		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
	}

	@Override
	public void render() {
		act(Gdx.graphics.getDeltaTime());
		camera3dController.update(Gdx.graphics.getDeltaTime());

		Gdx.gl.glClearColor(0, 0, 0, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);

		world.setDelta(MathUtils.clamp(Gdx.graphics.getDeltaTime(), 0f, 0.1f));
		world.process();
	}

	@Override
	public void resize(int width, int height) {
		sw = width;
		sh = height;

		ratio = (float) sh / (float) sw;

		vh = XY_BOUNDS;
		vw = vh / ratio;

		camera3d.viewportWidth = Gdx.graphics.getWidth();
		camera3d.viewportHeight = Gdx.graphics.getHeight();
		camera3d.update();
	}

	@Override
	public void dispose() {
		assetManager.dispose();
		modelBatch.dispose();
		world.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// camera3d.translate(0f, amount*10f, 0f);
		// camera3d.update();
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// camera3d.translate(-Gdx.input.getDeltaX()*stwr_x*speed, 0f,
		// -Gdx.input.getDeltaY()*stwr_y*speed);
		// camera3d.update();
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}
}
