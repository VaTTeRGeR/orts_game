package de.vatterger.engine.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;

/** Takes a {@link Camera} instance and controls it via w,a,s,d,q,e and mouse dragging for rotation.
 * @author badlogic */
public class RTSCameraController2D extends InputAdapter {
	private static final float MIN_ZOOM = 0.25f;
	private static final float MAX_ZOOM = 4f;
	
	private final Viewport viewport;
	private final Camera camera;
	private final Screen screen;
	
	private final IntIntMap keys = new IntIntMap();
	
	private int LEFT = Keys.A;
	private int RIGHT = Keys.D;
	private int FORWARD = Keys.W;
	private int BACKWARD = Keys.S;
	
	private int LEFT_ALT = Keys.LEFT;
	private int RIGHT_ALT = Keys.RIGHT;
	private int FORWARD_ALT = Keys.UP;
	private int BACKWARD_ALT = Keys.DOWN;

	private int UP = Keys.Q;
	private int DOWN = Keys.E;
	
	private float zoom = 1f;

	private Vector2 vec0 = new Vector2();
	private Vector2 vec1 = new Vector2();
	private Vector3 vec2 = new Vector3();
	
	public RTSCameraController2D (Viewport viewport, Screen screen) {
		this.viewport = viewport;
		this.camera = viewport.getCamera();
		this.screen = screen;
		setPosition(0f, 0f, 0f);
	}
	
	public void setPosition(float x, float y, float z) {
		camera.position.set(x, y, z);
		camera.update();
	}

	@Override
	public boolean keyDown (int keycode) {
		keys.put(keycode, keycode);
		return true;
	}

	@Override
	public boolean keyUp (int keycode) {
		keys.remove(keycode, 0);
		return true;
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
			float deltaX = Gdx.input.getDeltaX();
			float deltaY = Gdx.input.getDeltaY();
			viewport.unproject(vec0.set(screenX, screenY));
			viewport.unproject(vec1.set(screenX+deltaX, screenY+deltaY));
			vec0.sub(vec1);
			camera.position.add(vec0.x, vec0.y, 0f);
			camera.direction.rotate(Vector3.Z, deltaY);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean scrolled(int amount) {
		if(amount > 0)
			zoomOut();
		else
			zoomIn();
		
		return false;
	}

	public void update() {
		float delta = Gdx.graphics.getRawDeltaTime();
		
		if (keys.containsKey(FORWARD) || keys.containsKey(FORWARD_ALT)) {
			camera.position.y += 50f*delta*zoom;
			camera.update();
		} else if (keys.containsKey(BACKWARD) || keys.containsKey(BACKWARD_ALT)) {
			camera.position.y -= 50f*delta*zoom;
			camera.update();
		}
		
		if (keys.containsKey(LEFT) || keys.containsKey(LEFT_ALT)) {
			camera.position.x -= 50f*delta*zoom;
			camera.update();
		} else if (keys.containsKey(RIGHT) || keys.containsKey(RIGHT_ALT)) {
			camera.position.x += 50f*delta*zoom;
			camera.update();
		}
		
		if (Gdx.input.isKeyJustPressed(UP)) {
			zoomOut();
		} else if (Gdx.input.isKeyJustPressed(DOWN)) {
			zoomIn();
		}
		
		vec2.set(camera.position);

		camera.position.x = Math2D.round(camera.position.x, 10);
		camera.position.y = Math2D.round(camera.position.y, 10);

		camera.update();
		
		camera.position.set(vec2);
	}
	
	private void zoomIn(){
		zoom(0.5f);
	}
	
	private void zoomOut(){
		zoom(2f);
	}
	
	private void zoom(float amount) {
		zoom *= amount;
		zoom = Math.min(MAX_ZOOM, zoom);
		zoom = Math.max(MIN_ZOOM, zoom);
		screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	public void resize(int width, int height) {
		Metrics.wv = width;
		Metrics.hv = height;
		Metrics.ww = Metrics.wv * Metrics.mpp * zoom;
		Metrics.hw = Metrics.hv * Metrics.mpp * zoom;
		
		viewport.setWorldSize(Metrics.ww , Metrics.hw);
		viewport.update(Metrics.wv, Metrics.hv, false);
	}
}