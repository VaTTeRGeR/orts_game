package de.vatterger.tests;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.game.components.gameobject.Health;

public class QuaternionTest {

	static Vector3 v0 = new Vector3(Vector3.X);
	static Vector3 v1 = new Vector3(0.707f,0f,-0.707f).nor();
	static Vector3 v2 = new Vector3(Vector3.X);
	
	static Quaternion q0 = new Quaternion(Vector3.X, 0f);
	static Quaternion q1 = new Quaternion(Vector3.X, 0f);
	
	static Quaternion qX = new Quaternion();
	static Quaternion qY = new Quaternion();
	static Quaternion qZ = new Quaternion();
	
	public static void main(String[] args) {
		printVector(v0, 5f);
		printVector(v1, 5f);
		slerp(0.5f);
		for (int i = 0; i < 9; i++) {
			rotateQuat(0f, 0f, 10f);
		}
		
		Health h = new Health((short) 2);
	}
	
	private static void rotateQuat(float ax, float ay, float az) {
		qX.set(Vector3.X, ax);
		qY.set(Vector3.Y, ay);
		qZ.set(Vector3.Z, az);
		
		q0.mul(qX).mul(qY).mul(qZ);
		
		q0.transform(v0.set(Vector3.X));
		
		printVector(v0, 2.5f);
		
	}

	private static void slerp(float alpha) {
		v2.set(v0).slerp(v1, alpha);
		q1.setFromCross(v0, v2);
		
		q0.mul(q1);
		
		q0.transform(v0.set(Vector3.X));
		
		printVector(v0, 2.5f);
	}

	private static void printVector(Vector3 vector){
		printVector(vector, 1f);
	}
	
	private static void printVector(Vector3 vector, float scl){
		System.out.println("vektor(0|0|0 "+vector.x*scl+"|"+vector.y*scl+"|"+vector.z*scl+")");
	}
}