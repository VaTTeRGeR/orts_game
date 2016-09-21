package de.vatterger.engine.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class Math2D {
	private Math2D(){}
	public static int angleToIndex(float angle){
		return (int)((angle*8f/360f)%8f);
	}
	
	public static float indexToAngle(int index){
		return (float)((index*360/8)%360);
	}
	
	public static float roundAngleEight(float angle) {
		return indexToAngle(angleToIndex(angle));
	}
	
	public static float round(float value, float rounding) {
		return Math.round(value*rounding)/rounding;
	}
	
	public static void project(Vector3 v){
		v.y = Metrics.ymodp * (v.y + v.z);
		v.z = 0f;
	}

	public static void unproject(Vector3 v){
		v.y = Metrics.ymodu * v.y;
		v.z = 0f;
	}
	
	public static Vector3 castRay(Vector3 v, Camera camera) {
		camera.unproject(v);
		unproject(v);
		return v;
	}
	
	public static Vector3 castRayCam(Vector3 v, Camera camera) {
		v.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
		camera.unproject(v);
		unproject(v);
		return v;
	}
}
