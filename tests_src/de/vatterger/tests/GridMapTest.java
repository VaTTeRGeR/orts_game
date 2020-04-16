
package de.vatterger.tests;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.engine.handler.gridmap.GridMap2D;
import de.vatterger.engine.handler.gridmap.GridMapUtil;
import de.vatterger.engine.util.Profiler;

public class GridMapTest {

	public static void main (String[] args) throws InterruptedException {

		IntBag intBag = new IntBag(512);

		GridMap2D map = new GridMap2D(10f);

		Profiler p_put = new Profiler("put", TimeUnit.MICROSECONDS);
		
		for (int runs = 0; runs < 100; runs++) {
			
			p_put.start();
			
			map.clear();
				
			for (int i = 0; i < 10000; i++) {
				map.insertPoint(i, MathUtils.random(1000f), MathUtils.random(1000f), GridMapUtil.ALIVE);
			}
			
			p_put.log();
		}

		Profiler p = new Profiler("get", TimeUnit.NANOSECONDS);

		for (int j = 0; j < 100; j++) {

			float width = 100;
			
			Rectangle r = new Rectangle(MathUtils.random(1000 - width), MathUtils.random(1000 - width), width, width);

			p.start();

			for (int i = 0; i < 1000; i++) {

				intBag.setSize(0);

				map.getEntities(0, r, intBag);

				//System.out.println("Got " + intBag.size() + " entities!");
			}

			System.out.println(p.getTimeElapsed()/1000L + " ns");
		}

		System.out.println(GridMapUtil.toString(GridMapUtil.COLLISION));
		System.out.println(GridMapUtil.toString(GridMapUtil.AI));

		System.out.println(Arrays.toString(intBag.getData()));
	}
}
