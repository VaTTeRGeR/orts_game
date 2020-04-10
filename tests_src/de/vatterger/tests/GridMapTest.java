
package de.vatterger.tests;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;

import de.vatterger.engine.handler.gridmap.GridMap2D;
import de.vatterger.engine.handler.gridmap.GridMapUtil;
import de.vatterger.engine.util.Profiler;

public class GridMapTest {

	public static void main (String[] args) throws InterruptedException {

		IntBag intBag = new IntBag(6);

		GridMap2D map = new GridMap2D(1f);

		map.insertPoint(0, 0, 1, GridMapUtil.AI);
		map.insertPoint(1, 1, 2, GridMapUtil.AI);
		map.insertPoint(3, 2, 3, GridMapUtil.AI);
		map.insertPoint(0, 0, 4, GridMapUtil.ALIVE);
		map.insertPoint(1, 1, 5, GridMapUtil.COLLISION);
		map.insertPoint(3, 2, 6, GridMapUtil.NETWORKED);

		Profiler p = new Profiler("get", TimeUnit.NANOSECONDS);

		p.start();

		for (int j = 0; j < 10000; j++) {

			Circle c = new Circle(MathUtils.random(5f), MathUtils.random(5f), 0.25f);

			for (int i = 0; i < 10000; i++) {

				intBag.clear();

				map.getEntities(GridMapUtil.AI, c, intBag);

				//System.out.println("Got " + intBag.size() + " entities!");
			}
		}
		
		System.out.println(p.getTimeElapsed()/100000000L + " ns");

		System.out.println(GridMapUtil.toString(GridMapUtil.COLLISION));
		System.out.println(GridMapUtil.toString(GridMapUtil.AI));

		System.out.println(Arrays.toString(intBag.getData()));
	}
}
