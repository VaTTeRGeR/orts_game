package de.vatterger.techdemo.application;

import static de.vatterger.techdemo.application.GameConstants.XY_BOUNDS;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.camera.RTSCameraController;
import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.handler.network.ClientNetworkHandler;
import de.vatterger.engine.util.GameUtil;
import de.vatterger.techdemo.factory.client.TerrainFactory;
import de.vatterger.techdemo.lights.DirectionalShadowLight;
import de.vatterger.techdemo.network.packets.client.SpawnTankUpdate;
import de.vatterger.techdemo.processors.client.DrawFXModelProcessor;
import de.vatterger.techdemo.processors.client.DrawModelInfoProcessor;
import de.vatterger.techdemo.processors.client.DrawModelShadowProcessor;
import de.vatterger.techdemo.processors.client.DrawStaticModelsProcessor;
import de.vatterger.techdemo.processors.client.DrawTankModelProcessor;
import de.vatterger.techdemo.processors.client.InitPositionInterpolationProcessor;
import de.vatterger.techdemo.processors.client.InitRotationInterpolationProcessor;
import de.vatterger.techdemo.processors.client.InitTurretRotationInterpolationProcessor;
import de.vatterger.techdemo.processors.client.LocalMovementProcessor;
import de.vatterger.techdemo.processors.client.LocalVelocityProcessor;
import de.vatterger.techdemo.processors.client.MousePickingProcessor;
import de.vatterger.techdemo.processors.client.PositionInterpolationProcessor;
import de.vatterger.techdemo.processors.client.RemoteSlaveProcessor;
import de.vatterger.techdemo.processors.client.RotationInterpolationProcessor;
import de.vatterger.techdemo.processors.client.SendEntityAckProcessor;
import de.vatterger.techdemo.processors.client.SendViewportUpdateProcessor;
import de.vatterger.techdemo.processors.client.TurretRotationInterpolationProcessor;
import de.vatterger.techdemo.processors.shared.DeleteInactiveProcessor;
import de.vatterger.techdemo.processors.shared.DeleteTimedProcessor;

public class MainClient extends ApplicationAdapter implements InputProcessor {

	private World world;

	private int sw, sh;
	@SuppressWarnings("unused")
	private float vw, vh, ratio;

	private AssetManager assetManager;
	private ModelBatch modelBatch;
	private DecalBatch decalBatch;
	private Camera camera3d;
	private RTSCameraController camera3dController;
	private ImmediateModeRenderer20 imr20;
	private Environment environment;
	private DirectionalShadowLight shadowLight;
	private Color sky = new Color(186f / 255f, 232f / 255f, 236f / 255f, 1f);
	private Color sun = new Color(246f / 255f, 242f / 255f, 241f / 255f, 1f).mul(0.75f);
	private Color ambient = new Color(226f / 255f, 241f / 255f, 241f / 255f, 1f).mul(0.25f);

	private Decal decal;
	
	@SuppressWarnings("deprecation")
	@Override
	public void create() {
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, ambient));
		environment.set(new ColorAttribute(ColorAttribute.Fog, sky));
		environment.add(shadowLight = new DirectionalShadowLight(2048, 2048, 256, 256, 1, 512));
		shadowLight.set(sun, 0.2f, 1f, -1f);
		environment.shadowMap = shadowLight;
		
		camera3d = new PerspectiveCamera(90, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera3d.position.set(0f, 0f, 1.8f);
		camera3d.lookAt(0f, 10f, 1.8f);
		camera3d.near = 1f;
		camera3d.far = GameConstants.NET_SYNC_THRESHOLD;
		camera3d.update();

		camera3dController = new RTSCameraController(camera3d);
		camera3dController.setAcceleration(50f);
		camera3dController.setMaxVelocity(300f/3.6f);
		camera3dController.setDegreesPerPixel(0.5f);
		camera3dController.setHeightRestriction(12f, 224f);
		camera3dController.setAngleRestriction(30f, 89f);

		ModelHandler.loadModels(assetManager = new AssetManager());

		modelBatch = new ModelBatch();
		
		
		decalBatch = new DecalBatch(new CameraGroupStrategy(camera3d));
		decal = Decal.newDecal(new TextureRegion(new Texture("decal.png")), true);
		
		imr20 = new ImmediateModeRenderer20(false, true, 0);

		WorldConfiguration worldConfig = new WorldConfiguration();

		worldConfig.setSystem(new MousePickingProcessor(camera3d, imr20));
		worldConfig.setSystem(new RemoteSlaveProcessor());

		worldConfig.setSystem(new InitPositionInterpolationProcessor());
		worldConfig.setSystem(new InitRotationInterpolationProcessor());
		worldConfig.setSystem(new InitTurretRotationInterpolationProcessor());

		worldConfig.setSystem(new PositionInterpolationProcessor());
		worldConfig.setSystem(new RotationInterpolationProcessor());
		worldConfig.setSystem(new TurretRotationInterpolationProcessor());

		worldConfig.setSystem(new LocalVelocityProcessor());
		worldConfig.setSystem(new LocalMovementProcessor());

		worldConfig.setSystem(new DeleteInactiveProcessor(0f));
		worldConfig.setSystem(new DeleteTimedProcessor());

		worldConfig.setSystem(new DrawModelShadowProcessor(shadowLight, camera3d));
		worldConfig.setSystem(new DrawStaticModelsProcessor(modelBatch, camera3d, environment));
		worldConfig.setSystem(new DrawTankModelProcessor(modelBatch, camera3d, environment));
		worldConfig.setSystem(new DrawFXModelProcessor(modelBatch, camera3d, environment));
		worldConfig.setSystem(new DrawModelInfoProcessor(camera3d, environment));

		worldConfig.setSystem(new SendEntityAckProcessor());
		worldConfig.setSystem(new SendViewportUpdateProcessor(camera3d, imr20));

		world = new World(worldConfig);

		for (int x = 32; x <= XY_BOUNDS - 32; x += 64) {
			for (int y = 32; y <= XY_BOUNDS - 32; y += 64) {
				TerrainFactory.createTerrainTile(world, new Vector2(x, y));
			}
		}

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

		Gdx.gl.glClearColor(sky.r, sky.g, sky.b, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);

		world.setDelta(MathUtils.clamp(Gdx.graphics.getDeltaTime(), 0f, 0.1f));
		world.process();
		
		Vector3 ptr = GameUtil.intersectMouseGroundPlane(camera3d, Gdx.input.getX(), Gdx.input.getY());
		
		decal.setRotation(0f, 0f, 0f);

		decal.setDimensions(15f, 15f);
		decal.setPosition(ptr.x, ptr.y, ptr.z+0.01f);
		decalBatch.add(decal);

		decalBatch.flush();
		
		if(Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.LEFT)) {
			ClientNetworkHandler.instance().send(new SpawnTankUpdate(new Vector2(ptr.x, ptr.y)), true);
		}
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
		decalBatch.dispose();
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
	
	public static void main(String[] arg) {
		boolean fullscreen = false;
		if (!fullscreen) {
			LwjglApplicationConfiguration configWindow = new LwjglApplicationConfiguration();
			configWindow.width = 800;
			configWindow.height = 600;
			configWindow.samples = 4;
			configWindow.vSyncEnabled = false;
			configWindow.resizable = true;
			configWindow.title = "NETWORK DEMO";
			configWindow.fullscreen = false;
			configWindow.initialBackgroundColor = Color.BLACK;
			configWindow.allowSoftwareMode = false;
			configWindow.addIcon("icon32.png", FileType.Internal);
			configWindow.backgroundFPS = 30;
			configWindow.foregroundFPS = 120;
			new LwjglApplication(new MainClient(), configWindow);
		} else {
			LwjglApplicationConfiguration configWindowedFullscreen = new LwjglApplicationConfiguration();
			System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
			configWindowedFullscreen.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
			configWindowedFullscreen.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
			configWindowedFullscreen.samples = 4;
			configWindowedFullscreen.vSyncEnabled = false;
			configWindowedFullscreen.fullscreen = true;
			configWindowedFullscreen.initialBackgroundColor = Color.BLACK;
			configWindowedFullscreen.allowSoftwareMode = false;
			configWindowedFullscreen.backgroundFPS = 30;
			configWindowedFullscreen.foregroundFPS = 120;
			new LwjglApplication(new MainClient(), configWindowedFullscreen);
		}
	}
}
