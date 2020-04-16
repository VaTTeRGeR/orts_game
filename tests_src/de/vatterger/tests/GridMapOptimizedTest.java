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

		IntBag intBag = new IntBag(512);
		
		GridMapOptimized2D map = new GridMapOptimized2D(100/*cells*/, 10/*cell width and height*/, 4/*initial cell storage space*/, true/*pre-allocated and ordered*/);

		Profiler p_put = new Profiler("put", TimeUnit.MICROSECONDS);
		
		for (int runs = 0; runs < 100; runs++) {
		
			p_put.start();
			
			map.clear();
			
			for (int i = 0; i < 10000; i++) {
				map.put(i, MathUtils.random(1000f), MathUtils.random(1000f), 0, GridMapUtil.ALIVE);
			}
			
			p_put.log();
		}
		
		Profiler p_get = new Profiler("get", TimeUnit.NANOSECONDS);

		for (int j = 0; j < 100; j++) {

			float width = 100;
			
			float x1 = MathUtils.random(1000f - width);
			float y1 = MathUtils.random(1000f - width);
			float x2 = x1 + width;
			float y2 = y1 + width;
			
			p_get.start();

			for (int i = 0; i < 1000; i++) {

				intBag.setSize(0);

				map.getIdOnly(x1, y1, x2, y2, 0, intBag);

				//System.out.println("Got " + intBag.size() + " entities!");
			}

			System.out.println(p_get.getTimeElapsed()/1000L + " ns");
		}

		System.out.println(GridMapUtil.toString(GridMapUtil.COLLISION));
		System.out.println(GridMapUtil.toString(GridMapUtil.AI));

		System.out.println(Arrays.toString(intBag.getData()));
	}
}
