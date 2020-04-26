package de.vatterger.game.systems.gameplay;

import java.util.concurrent.TimeUnit;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.FloatArray;

import de.vatterger.engine.handler.gridmap.GridMapOptimized2D;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CollisionRadius;
import de.vatterger.game.components.gameobject.StaticObject;
import de.vatterger.game.systems.graphics.GraphicalProfilerSystem;

public class DynamicObjectMapSystem extends BaseEntitySystem {

	private static DynamicObjectMapSystem SELF;
	
	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<CollisionRadius> crm;
	
	private final IntBag insertedBag = new IntBag(1024);
	private final IntBag removedBag = new IntBag(1024);
	
	private final GridMapOptimized2D gridMap;

	private Profiler profiler = new Profiler("DynamicObjectMapSystem", TimeUnit.MICROSECONDS);
	
	public DynamicObjectMapSystem() {
		
		super(Aspect.all(AbsolutePosition.class, CollisionRadius.class, StaticObject.class).exclude(StaticObject.class));

		if(SELF != null) throw new IllegalStateException("More than one instance of Singleton StaticObjectMapSystem detected.");
		
		SELF = this;
		
		gridMap = new GridMapOptimized2D(100, 20, 10, true);
		
		GraphicalProfilerSystem.registerProfiler("DynamicObjectMapSystem", Color.YELLOW, profiler);
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
		
		synchronized (gridMap) {
		
			if(insertedBag.size() > 0) {
				int[]	entityIds		= insertedBag.getData();
				int	entityIds_size	= insertedBag.size();
				
				for (int i = 0; i < entityIds_size; i++) {
		
					final int entityId = entityIds[i];
					
					AbsolutePosition ap = apm.get(entityId);
					CollisionRadius cr = crm.get(entityId);
					
					gridMap.put(entityId, ap.position.x + cr.offsetX, ap.position.y + cr.offsetY, cr.dst);
				}
				
				insertedBag.setSize(0);
			}

			if(removedBag.size() > 0) {
				
				for (int entityId : removedBag.getData()) {
					gridMap.remove(entityId);
				}
				
				removedBag.setSize(0);
			}
			
			int[] entityIds		= getEntityIds().getData();
			int entityIds_size	= getEntityIds().size();
			
			for (int i = 0; i < entityIds_size; i++) {
	
				final int entityId = entityIds[i];
				
				AbsolutePosition ap = apm.get(entityId);
				CollisionRadius cr = crm.get(entityId);
				
				gridMap.update(entityId, ap.position.x + cr.offsetX, ap.position.y + cr.offsetY);
			}
		}
	}
	
	@Override
	protected void end () {
		profiler.stop();
	}

	public static float[] getData(float x1,float y1, float x2, float y2) {
		
		final FloatArray fillArray = new FloatArray(512);
		
		// placeholder for size variable
		fillArray.add(0);
		
		synchronized (SELF.gridMap) {
			SELF.gridMap.get(x1, y1, x2, y2, 0, null, fillArray);
		}
	
		fillArray.set(0, (fillArray.size - 1) / 3);
		
		return fillArray.items;
	}
}
