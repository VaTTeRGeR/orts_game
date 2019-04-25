package de.vatterger.engine.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Math2D {
	
	private Math2D(){}
	
	public static float atan2d(float y, float x) {
		return normalize_360(MathUtils.atan2(y, x) * MathUtils.radDeg + 90f);
	}
	
	public static float normalize(float value, float start, float end) {
		float width = end - start;
		float offset = value - start;
		
		return (offset - MathUtils.floor(offset/width) * width) + start;
	}

	public static float normalize_360(float value) {
		float _value = value % 360f;
		return _value >= 0f ? _value : 360f + _value;
	}

	public static int angleToIndex(float angle, int numAngles) {
		return (int)(((angle*numAngles)/360f + 0.5f) % numAngles);
	}
	
	public static float indexToAngle(int index, int numAngles) {
		return (float)((index*360/numAngles)%360);
	}
	
	public static float roundAngle(float angle, int numAngles) {
		return indexToAngle(angleToIndex(angle, numAngles), numAngles);
	}
	
	public static float round(float value, float rounding) {
		return Math.round(value*rounding)/rounding;
	}
	
	public static Vector3 project(Vector3 v) {
		v.y = Metrics.ymodp * (v.y + v.z);
		v.z = 0f;
		return v;
	}

	public static Vector2 project(Vector2 v) {
		v.y = Metrics.ymodp * v.y;
		return v;
	}

	public static Vector3 unproject(Vector3 v) {
		v.y = Metrics.ymodu * v.y;
		v.z = 0f;
		return v;
	}
	
	public static Vector2 unproject(Vector2 v) {
		v.y = Metrics.ymodu * v.y;
		return v;
	}
	
	public static Vector3 castRay(Vector3 v, Camera camera) {
		camera.unproject(v);
		unproject(v);
		return v;
	}
	
	/**
	 * @return The parameter v set to the world position of where the mouse points.*/
	public static Vector3 castMouseRay(Vector3 v, Camera camera) {
		v.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
		camera.unproject(v);
		unproject(v);
		return v;
	}
}
