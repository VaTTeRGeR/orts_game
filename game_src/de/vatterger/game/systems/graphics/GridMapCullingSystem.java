package de.vatterger.game.systems.graphics;

import java.util.concurrent.TimeUnit;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntityManager;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.gridmap.GridMapFlag;
import de.vatterger.engine.handler.gridmap.GridMapQuery;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.NotCulled;
import de.vatterger.game.systems.gameplay.DynamicObjectMapSystem;
import de.vatterger.game.systems.gameplay.StaticObjectMapSystem;

public class GridMapCullingSystem extends BaseSystem {

	private ComponentMapper<AbsolutePosition>	pm;
	private ComponentMapper<CullDistance>		cdm;
	private ComponentMapper<NotCulled>			ncm;
	
	@Wire(name="camera")
	private Camera camera;

	private final GridMapQuery query = new GridMapQuery(4096, true, false);
	
	private final Rectangle r0 = new Rectangle();
	private final Rectangle r1 = new Rectangle();

	private float x1,y1,x2,y2;

	private Profiler profiler = new Profiler("GridMapCullingSystem", TimeUnit.MICROSECONDS);
	
	public GridMapCullingSystem() {
		GraphicalProfilerSystem.registerProfiler("GridMapCullingSystem", Color.CORAL, profiler);
	}
	
	@Override
	protected void begin() {

		profiler.start();
		
		r0.setSize(camera.viewportWidth, camera.viewportHeight);
		r0.setCenter(camera.position.x, camera.position.y);
		
		r0.y = r0.y * Metrics.ymodu;
		r0.height = r0.height * Metrics.ymodu;
		
		x1 = r0.x;
		y1 = r0.y;
		x2 = r0.x + r0.width;
		y2 = r0.y + r0.height;
		
		query.clear();
	}

	@Override
	protected void processSystem () {
		
		//Profiler p = new Profiler("GET DATA FROM GRIDMAPS", TimeUnit.MICROSECONDS);
		
		DynamicObjectMapSystem.getData(x1, y1, x2, y2, 0, query);
		StaticObjectMapSystem.getData(x1, y1, x2, y2, 0, query);
		
		//p.log();
		
		final int size = query.getSize();
		final int[] ids = query.getIdData();
		
		for (int i = 0; i < size; i++) {
			
			final int entityId = ids[i];
			
			// No need to check it if it's already in NotCulled state.
			if(!ncm.has(entityId)) {
			
				final Vector3			pos	= pm.get(entityId).position;
				final CullDistance	cd		= cdm.get(entityId);
				
				r1.setSize(cd.dst * 2f, cd.dst * 2f);
				r1.setCenter(pos.x + cd.offsetX, pos.y + cd.offsetY);
				
				if(r0.overlaps(r1)) {
					//System.out.println("Added NotCulled to " + entityId);
					world.edit(entityId).remove(Culled.class).add(NotCulled.flyweight);
				}
			}
		}
	}
	
	@Override
	protected void end() {
		profiler.stop();
	}
}
