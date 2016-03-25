package de.vatterger.entitysystem.camera;

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
	private int UP = Keys.Q;
	private int DOWN = Keys.E;

	private float minHeight = 16f;
	private float maxHeight = 128f;
	private float cameraAngle = 30f;

	private float moveBorderSize = 64f;

	private float degreesPerPixel = 0.5f;
	private float acceleration = 200f;
	private float velocity = 5f;
	
	private float alpha = 1f;
	private float beta = 1f;
	
	private final Vector3 velocityXYZ = new Vector3();
	private final Vector3 tmp = new Vector3();

	public RTSCameraController (Camera camera) {
		this.camera = camera;
		camera.direction.set(Vector3.Z).scl(-1f).rotate(Vector3.X, cameraAngle);
		camera.position.set(0, 0, 64f);
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
		cameraAngle = angle;
	}

	public void setHeightRestriction(float min, float max) {
		minHeight = min;
		maxHeight = max;
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
		float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
		if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
			camera.direction.rotate(Vector3.Z, deltaX);
			camera.direction.rotate(Vector3.Z, deltaY);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean scrolled(int amount) {
		velocityXYZ.add(0f, 0f, 20f*amount);
		resetBeta();
		return true;
	}

	public void update () {
		update(Gdx.graphics.getDeltaTime());
	}

	public void update (float deltaTime) {
		//decrease alpha/beta linearly to fade camera movement
		alpha = MathUtils.clamp(alpha - deltaTime, 0f, 1f);
		beta = MathUtils.clamp(beta - deltaTime, 0f, 1f);

		if (keys.containsKey(FORWARD) || Gdx.input.getY() < moveBorderSize) {
			getCameraXYDirection(tmp).scl(acceleration*deltaTime);
			velocityXYZ.add(tmp);
			resetAlpha();
		}
		if (keys.containsKey(BACKWARD) || Gdx.input.getY() > Gdx.graphics.getHeight() - moveBorderSize) {
			getCameraXYDirection(tmp).scl(-acceleration*deltaTime);
			velocityXYZ.add(tmp);
			resetAlpha();
		}
		if (keys.containsKey(LEFT) || Gdx.input.getX() < moveBorderSize) {
			getCameraXYDirection(tmp).scl(acceleration*deltaTime).rotate(Vector3.Z, 90f);
			velocityXYZ.add(tmp);
			resetAlpha();
		}
		if (keys.containsKey(RIGHT) || Gdx.input.getX() > Gdx.graphics.getWidth() - moveBorderSize) {
			getCameraXYDirection(tmp).scl(acceleration*deltaTime).rotate(Vector3.Z, -90f);
			velocityXYZ.add(tmp);
			resetAlpha();
		}
		if (keys.containsKey(UP)) {
			tmp.set(Vector3.Z).scl(acceleration*deltaTime);
			velocityXYZ.add(tmp);
			resetBeta();
		}
		if (keys.containsKey(DOWN)) {
			tmp.set(Vector3.Z).scl(-acceleration*deltaTime);
			velocityXYZ.add(tmp);
			resetBeta();
		}

		float velZ = velocityXYZ.z;
		//exponential decelleration
		velocityXYZ.clamp(0f, velocity*Interpolation.exp10In.apply(alpha));
		velocityXYZ.z = MathUtils.clamp(velZ,-velocity*Interpolation.exp10In.apply(beta),velocity*Interpolation.exp10In.apply(beta));
		
		camera.position.add(tmp.set(velocityXYZ).scl(deltaTime));
		camera.position.z = MathUtils.clamp(camera.position.z, minHeight, maxHeight);
		camera.update(true);
	}
	
	private void resetAlpha() {
		alpha = 1f;
	}

	private void resetBeta() {
		beta = 1f;
	}

	private Vector3 getCameraXYDirection(Vector3 vec) {
		return vec.set(camera.direction.x, camera.direction.y, 0).nor();
	}
}
