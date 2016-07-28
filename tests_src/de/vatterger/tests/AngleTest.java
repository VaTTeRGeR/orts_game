package de.vatterger.tests;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class AngleTest {

	public static void main(String[] args) {
		Quaternion q1 = new Quaternion(Vector3.Z, -90f);
		Quaternion q2 = new Quaternion(Vector3.Z, 90f);
		System.out.println("Angle "+q1.getAngleAround(Vector3.Z));
		System.out.println("Angle "+q2.getAngleAround(Vector3.Z));
	}

}
