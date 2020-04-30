package de.vatterger.game.systems.gameplay;

import java.util.concurrent.TimeUnit;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.Color;

import de.vatterger.engine.handler.gridmap.GridMap2DField;
import de.vatterger.engine.handler.gridmap.GridMapQuery;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CollisionRadius;
import de.vatterger.game.components.gameobject.StaticObject;
import de.vatterger.game.systems.graphics.GraphicalProfilerSystem;

public class StaticObjectMapSystem extends BaseEntitySystem {

	private static StaticObjectMapSystem SELF;
	
	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<CollisionRadius> crm;
	
	private final GridMap2DField gridMap;

	private final IntBag insertedBag = new IntBag(1024);
	private final IntBag removedBag = new IntBag(1024);
	
	private Profiler profiler = new Profiler("StaticObjectMapSystem", TimeUnit.MICROSECONDS);
	
	public StaticObjectMapSystem() {
		
		super(Aspect.all(AbsolutePosition.class, CollisionRadius.class, StaticObject.class));

		if(SELF != null) throw new IllegalStateException("More than one instance of Singleton StaticObjectMapSystem detected.");
		
		SELF = this;
		
		gridMap = new GridMap2DField(100, 10, 20, 4);
		
		GraphicalProfilerSystem.registerProfiler("StaticObjectMapSystem", Color.YELLOW, profiler);
	}
	
	@Override
	protected void inserted (int entityId) {
		insertedBag.add(entityId);
	}
	
	@Override
	protected void removed (int entityId) {
		removedBag.add(entityId);
	}
	
	@Override
	protected void begin () {
		profiler.start();
	}
	
	@Override
	protected void processSystem () {
		
		if(insertedBag.size() == 0 && removedBag.size() == 0) {
			return;
		}
		
		synchronized (gridMap) {
		
			final int[]	entityIds		= insertedBag.getData();
			final int	entityIds_size	= insertedBag.size();
			
			for (int i = 0; i < entityIds_size; i++) {
	
				final int entityId = entityIds[i];
				
				AbsolutePosition ap = apm.get(entityId);
				CollisionRadius cr = crm.get(entityId);
				
				gridMap.put(entityId, ap.position.x + cr.offsetX, ap.position.y + cr.offsetY, cr.dst);
			}
			
			for (int entityId : removedBag.getData()) {
				gridMap.remove(entityId);
			}
			
			insertedBag.setSize(0);
			removedBag.setSize(0);
		}
	}
	
	@Override
	protected void end () {
		profiler.stop();
		
		//System.out.println(Arrays.toString(gridMap.getFreeSpaceDistribution()));
	}

	public static void getData(float x1,float y1, float x2, float y2, GridMapQuery result) {
		
		synchronized (SELF.gridMap) {
			SELF.gridMap.get(x1, y1, x2, y2, 0, result);
		}
	}
}
