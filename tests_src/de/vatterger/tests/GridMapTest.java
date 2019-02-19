package de.vatterger.tests;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.Circle;

import de.vatterger.engine.handler.gridmap.GridMapHandler;
import de.vatterger.engine.handler.gridmap.GridMapUtil;
import de.vatterger.engine.util.Profiler;

public class GridMapTest {

	public static void main(String[] args) throws InterruptedException {
		IntBag intBag = new IntBag(6);
		
		GridMapHandler.init(1f);
		
		GridMapHandler.insertPoint(0, 0, 1, GridMapUtil.AI);
		GridMapHandler.insertPoint(1, 1, 2, GridMapUtil.AI);
		GridMapHandler.insertPoint(3, 2, 3, GridMapUtil.AI);
		GridMapHandler.insertPoint(0, 0, 4, GridMapUtil.ALIVE);
		GridMapHandler.insertPoint(1, 1, 5, GridMapUtil.COLLISION);
		GridMapHandler.insertPoint(3, 2, 6, GridMapUtil.NETWORKED);
		
		Profiler p = new Profiler("get", TimeUnit.NANOSECONDS);

		for (int i = 0; i < 10000; i++) {
			intBag.clear();
			
			p.start();
			
			Circle c = new Circle(0.9f, 0.9f, 0.25f);
			
			GridMapHandler.getEntities(GridMapUtil.ALIVE, c, intBag);
			GridMapHandler.getEntities(GridMapUtil.AI,    c, intBag);
			
			p.log();
		}
		
		System.out.println(Arrays.toString(intBag.getData()));
	}

}
