package de.vatterger.engine.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public final class Math2D {
	
	private Math2D(){}
	
	public final static float atan2d(float y, float x) {
		return normalize_360(MathUtils.atan2(y, x) * MathUtils.radDeg + 90f);
	}
	
	public final static float normalize(float value, float start, float end) {

		final float width = end - start;
		
		final float offset = value - start;
		
		return (offset - MathUtils.floor(offset / width) * width) + start;
	}

	public final static float normalize_360(float value) {
		
		value %= 360f;
		
		if(value < 0f) {
			value += 360f;
		}
		
		return value;
	}

	public final static int angleToIndex(float angle, float numAngleSteps) {
		return (int) ( ((angle * numAngleSteps) / 360f + 0.5f) % numAngleSteps );
	}
	
	public final static float indexToAngle(float index, float numAngleSteps) {
		return (index * 360f / numAngleSteps) % 360f;
	}
	
	public final static float roundAngle(float angle, float numAngleSteps) {
		return indexToAngle(angleToIndex(angle, numAngleSteps), numAngleSteps);
	}
	
	public final static float round(float value, float rounding) {
		return Math.round(value * rounding) / rounding;
	}
	
	public final static Vector2 project(Vector2 v) {
		return v.set(v.x, v.y * Metrics.ymodp);
	}

	public final static Vector3 project(Vector3 v) {
		return v.set(v.x, (v.y + v.z) * Metrics.ymodp, 0f);
	}

	public final static Rectangle project(Rectangle r) {
		
		r.y = r.y * Metrics.ymodp;
		r.height = r.height * Metrics.ymodp;
		
		return r;
	}
	
	public final static Vector2 unproject(Vector2 v) {
		return v.set(v.x, v.y * Metrics.ymodu);
	}
	
	public final static Vector3 unproject(Vector3 v) {
		return v.set(v.x, v.y * Metrics.ymodu, 0f);
	}
	
	public final static Rectangle unproject(Rectangle r) {
		
		r.y = r.y * Metrics.ymodu;
		r.height = r.height * Metrics.ymodu;
		
		return r;
	}
	
	public final static Vector3 castRay(Vector3 v, Camera camera) {

		camera.unproject(v);
		
		unproject(v);
		
		return v;
	}
	
	/**
	 * @return The parameter v set to the world position of where the mouse points.*/
	public final static Vector3 castMouseRay(Vector3 v, Camera camera) {
		
		v.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
		
		camera.unproject(v);
		
		unproject(v);

		return v;
	}
}
