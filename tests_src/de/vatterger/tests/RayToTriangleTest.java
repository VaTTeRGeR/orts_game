package de.vatterger.tests;

import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import de.vatterger.engine.util.Profiler;

public class RayToTriangleTest {
	public static void main(String[] args) {
		Ray ray = new Ray();
		Vector3 intersectionNew = new Vector3();
		Vector3 intersectionOld = new Vector3();
		
		float[] triangles = new float[]{1,1,1,0,0,0,2,0,0};
		
		for (int j = 0; j < 100; j++) {
			Profiler p = new Profiler("Triangle-Ray Intersect", TimeUnit.MICROSECONDS);

			for (int i = 0; i < 100000; i++) {
				ray.set(intersectionOld.add(MathUtils.random()),
						new Vector3(MathUtils.random(), MathUtils.random(), MathUtils.random()).nor());
				if (Intersector.intersectRayTriangles(ray, triangles, intersectionNew)) {
					System.out.println("hit!");
					intersectionOld.set(intersectionNew);
				}
			}

			p.log();
		}
	}
}
