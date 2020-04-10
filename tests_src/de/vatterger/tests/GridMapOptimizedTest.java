package de.vatterger.tests;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.MathUtils;

import de.vatterger.engine.handler.gridmap.GridMapOptimized2D;
import de.vatterger.engine.handler.gridmap.GridMapUtil;
import de.vatterger.engine.util.Profiler;

public class GridMapOptimizedTest {

	public static void main (String[] args) throws InterruptedException {

		IntBag intBag = new IntBag(6);

		GridMapOptimized2D map = new GridMapOptimized2D(4,1,16,1);

		map.put(0, 0, 0, 1, GridMapUtil.AI);
		map.put(1, 1, 0, 2, GridMapUtil.AI);
		map.put(3, 2, 0, 3, GridMapUtil.AI);
		map.put(0, 0, 0, 4, GridMapUtil.ALIVE);
		map.put(1, 1, 0, 5, GridMapUtil.COLLISION);
		map.put(3, 2, 0, 6, GridMapUtil.NETWORKED);
		
		Profiler p = new Profiler("get", TimeUnit.NANOSECONDS);

		p.start();

		for (int j = 0; j < 10000; j++) {

			float x1 = MathUtils.random(0f, 4.75f);
			float y1 = MathUtils.random(0f, 4.75f);
			float x2 = x1 + 0.5f;
			float y2 = y1 + 0.5f;
			
			for (int i = 0; i < 10000; i++) {

				intBag.clear();

				map.getIdOnly(x1, y1, x2, y2, GridMapUtil.AI, intBag);

				//System.out.println("Got " + intBag.size() + " entities!");
			}
		}

		System.out.println(p.getTimeElapsed()/100000000L + " ns");

		System.out.println(GridMapUtil.toString(GridMapUtil.COLLISION));
		System.out.println(GridMapUtil.toString(GridMapUtil.AI));

		System.out.println(Arrays.toString(intBag.getData()));
	}
}
