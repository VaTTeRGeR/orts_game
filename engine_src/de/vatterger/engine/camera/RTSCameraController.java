package de.vatterger.engine.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;

/** Takes a {@link Camera} instance and controls it via w,a,s,d,q,e and mouse dragging for rotation.
 * @author badlogic */
public class RTSCameraController extends InputAdapter {
	private final Camera camera;
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

	private float minHeight = 8f;
	private float maxHeight = 256f;
	private float minCameraAngle = 30f;
	private float maxCameraAngle = 60f;

	private float moveBorderSize = 64f;

	private float degreesPerPixel = 0.5f;
	private float acceleration = 200f;
	private float velocity = 5f;
	
	private float alpha = 1f;
	
	private final Vector3 velocityXYZ = new Vector3();
	private final Vector3 tmp = new Vector3();

	public RTSCameraController (Camera camera) {
		this.camera = camera;
		setPosition(0f, 0f, 0f);
		setDirection(1f, 1f);
	}
	
	public void setPosition(float x, float y, float z) {
		camera.position.set(x, y, z);
		camera.update();
	}

	public void setDirection(float dx, float dy) {
		if((dx+dy) != 0f || (dx-dy) != 0f) {
			camera.up.set(0f, 0f, 1f).nor();
			camera.direction.set(dx, dy, 0f).nor();
			camera.update();
		}
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

	/** Sets the velocity in units per second for moving forward, backward and strafing left/right as well as upwards/downwards.
	 * @param velocity the velocity in units per second */
	public void setMaxVelocity (float velocity) {
		this.velocity = velocity;
	}

	/** Sets the acceleration in units per second for moving forward, backward and strafing left/right as well as upwards/downwards.
	 * @param acceleration the acceleration in units per second */
	public void setAcceleration (float acceleration) {
		this.acceleration = acceleration;
	}

	/** Sets how many degrees to rotate around z-axis per pixel the mouse moved.
	 * @param degreesPerPixel */
	public void setDegreesPerPixel (float degreesPerPixel) {
		this.degreesPerPixel = degreesPerPixel;
	}

	public void setCameraAngle(float angle) {
		camera.direction.z = 0f;
		camera.direction.nor().rotate(getCameraLeftVector(tmp), angle).nor();
	}

	public void setHeightRestriction(float min, float max) {
		minHeight = min;
		maxHeight = max;
	}

	public void setPitchAngleRestriction(float min, float max) {
		if (min <= max) {
			minCameraAngle = MathUtils.clamp(min, -89f, 89f);
			maxCameraAngle = MathUtils.clamp(max, -89f, 89f);
		}
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT) || Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
			float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
			float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
			camera.direction.rotate(Vector3.Z, deltaX);
			camera.direction.rotate(Vector3.Z, deltaY);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean scrolled(int amount) {
		camera.position.add(0f, 0f, 10f*amount);
		resetAlpha();
		return true;
	}

	public void update () {
		update(Gdx.graphics.getDeltaTime());
	}

	public void update (float deltaTime) {
		//decrease alpha/beta linearly to damp camera movement (final damp = interpolation.apply(alpha))
		alpha = MathUtils.clamp(alpha - 2*deltaTime, 0f, 1f);
		
		if (keys.containsKey(FORWARD) || keys.containsKey(FORWARD_ALT) || Gdx.input.getY() < moveBorderSize) {
			getCameraForwardVector(tmp).scl(acceleration*deltaTime);
			velocityXYZ.add(tmp);
			resetAlpha();
		} else if (keys.containsKey(BACKWARD) || keys.containsKey(BACKWARD_ALT) || Gdx.input.getY() > Gdx.graphics.getHeight() - moveBorderSize) {
			getCameraForwardVector(tmp).scl(-acceleration*deltaTime);
			velocityXYZ.add(tmp);
			resetAlpha();
		} else if (keys.containsKey(LEFT) || keys.containsKey(LEFT_ALT) || Gdx.input.getX() < moveBorderSize) {
			getCameraLeftVector(tmp).scl(acceleration*deltaTime);
			velocityXYZ.add(tmp);
			resetAlpha();
		} else if (keys.containsKey(RIGHT) || keys.containsKey(RIGHT_ALT) || Gdx.input.getX() > Gdx.graphics.getWidth() - moveBorderSize) {
			getCameraLeftVector(tmp).scl(-acceleration*deltaTime);
			velocityXYZ.add(tmp);
			resetAlpha();
		}
		if (keys.containsKey(UP)) {
			tmp.set(camera.direction).nor().scl(-acceleration*deltaTime);
			velocityXYZ.add(tmp);
			resetAlpha();
		} else if (keys.containsKey(DOWN)) {
			tmp.set(camera.direction).nor().scl(acceleration*deltaTime);
			velocityXYZ.add(tmp);
			resetAlpha();
		}

		velocityXYZ.clamp(0f, velocity*Interpolation.pow5In.apply(alpha));

		camera.position.add(tmp.set(velocityXYZ).scl(deltaTime));
		camera.position.z = MathUtils.clamp(camera.position.z, minHeight, maxHeight);
		
		if(camera.position.z == minHeight || camera.position.z == maxHeight) {
			velocityXYZ.z = 0f;
		}
		
		setCameraAngle(Interpolation.pow2Out.apply(minCameraAngle, maxCameraAngle, MathUtils.clamp(camera.position.z/maxHeight, 0f, 1f)));
		//setCameraAngle(minCameraAngle);

		camera.update(true);
	}
	
	private void resetAlpha() {
		alpha = 1f;
	}

	private Vector3 getCameraForwardVector(Vector3 vec) {
		return vec.set(camera.direction.x, camera.direction.y, 0).nor();
	}

	private Vector3 getCameraLeftVector(Vector3 vec) {
		return vec.set(camera.direction.x, camera.direction.y, 0).nor().rotate(Vector3.Z, 90f);
	}
}
