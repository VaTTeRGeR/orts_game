package de.vatterger.engine.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;

/** Takes a {@link Camera} instance and controls it via w,a,s,d,q,e and mouse dragging for rotation.
 * @author badlogic */
public class RTSCameraController2D extends InputAdapter {
	
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

	private int ZOOM_OUT = Keys.PAGE_UP;
	private int ZOOM_IN = Keys.PAGE_DOWN;
	
	private boolean isTouchDown = false;
	
	private static final float MIN_ZOOM = 1f;
	private static final float MAX_ZOOM = 16f;
	
	private float zoom = 1f;

	private Vector2 vec0 = new Vector2();
	private Vector2 vec1 = new Vector2();
	private Vector3 vec2 = new Vector3();
	private Vector3 vec3 = new Vector3();

	private Vector3 camPos = new Vector3();
	
	private Vector3 previousMousePos = new Vector3();
	private Vector3 currentMousePos = new Vector3();
	
	public RTSCameraController2D (Viewport viewport, Screen screen) {
		
		this.viewport = viewport;
		this.camera = viewport.getCamera();
		this.screen = screen;
		
		setPosition(0f, 0f, 0f);
	}
	
	public void setPosition(Vector3 pos) {
		setPosition(pos.x, pos.y, pos.z);
	}
	
	public void setPosition(float x, float y, float z) {
		
		camPos.set(x, y, z);
		
		//Math2D.project(vec2.set(camPos));
		
		//camera.position.set(vec2);
		//camera.update();
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

	// Gdx.input.getDeltaX does not work properly on Ubuntu 18.04!
	/*@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
			float deltaX = Gdx.input.getDeltaX();
			float deltaY = Gdx.input.getDeltaY();
			viewport.unproject(vec0.set(screenX, screenY));
			viewport.unproject(vec1.set(screenX+deltaX, screenY+deltaY));
			Math2D.unproject(vec0);
			Math2D.unproject(vec1);

			vec0.sub(vec1);

			setPosition(camPos.add(vec0.x, vec0.y, 0f));
			
			return true;
		}
		return false;
	}*/
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(button == Input.Buttons.RIGHT) {
			isTouchDown = true;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(button == Input.Buttons.RIGHT) {
			isTouchDown = false;
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
		
		return true;
	}

	public void update(float delta) {
		
		currentMousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
		
		if(isTouchDown) {
			
			vec2.set(currentMousePos).sub(previousMousePos);
			
			float deltaX = vec2.x;
			float deltaY = vec2.y;
			
			viewport.unproject(vec0.set(0f, 0f));
			viewport.unproject(vec1.set(deltaX, deltaY));
			
			Math2D.unproject(vec0);
			Math2D.unproject(vec1);
			
			vec0.sub(vec1);
			
			camPos.add(vec0.x, vec0.y, 0f);
		}
		
		previousMousePos.set(currentMousePos);
		
		
		if (keys.containsKey(FORWARD) || keys.containsKey(FORWARD_ALT)) {
			camPos.y += zoom * 50f * delta;
		} else if (keys.containsKey(BACKWARD) || keys.containsKey(BACKWARD_ALT)) {
			camPos.y -= zoom * 50f * delta;
		}
		
		if (keys.containsKey(LEFT) || keys.containsKey(LEFT_ALT)) {
			camPos.x -= zoom * 50f * delta;
		} else if (keys.containsKey(RIGHT) || keys.containsKey(RIGHT_ALT)) {
			camPos.x += zoom * 50f * delta;
		}
		
		if (Gdx.input.isKeyPressed(ZOOM_OUT)) {
			zoomOut();
		} else if (Gdx.input.isKeyPressed(ZOOM_IN)) {
			zoomIn();
		}
		
		// Store value in working vector
		vec2.set(camPos);
		
		camera.position.set(Math2D.project(vec2));
		
		// Assures camera pixels are aligned to sprite pixels, assumes everything is rendered at absolute pixel positions
		camera.position.x = Math2D.round(camera.position.x, Metrics.ppm);
		camera.position.y = Math2D.round(camera.position.y, Metrics.ppm);
		
		camera.update();
	}
	
	private void moveCenterTowardsCursor() {
		
		vec3.set(camPos);
		
		if(zoom - MIN_ZOOM > 0f)
			setPosition(Math2D.castMouseRay(vec2, camera).interpolate(vec3, 0.5f, Interpolation.linear));
		else
			setPosition(Math2D.castMouseRay(vec2, camera).interpolate(vec3, 0.75f, Interpolation.linear));
	}
	
	private void zoomIn(){
		
		moveCenterTowardsCursor();
		
		zoom = Math.max(zoom / 2f, MIN_ZOOM);
		
		// Triggers a recalculation of the viewport in the GameScreen.
		screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	private void zoomOut(){
		
		zoom = Math.min(zoom * 2f, MAX_ZOOM);

		// Triggers a recalculation of the viewport in the GameScreen.
		screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	/**
	 * @return The zoom-out multiplier, larger means the ground appears farther away.
	 */
	public float getZoom() {
		return zoom;
	}
}
