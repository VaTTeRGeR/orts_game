package de.vatterger.game.systems.gameplay;

import java.util.concurrent.TimeUnit;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.Color;

import de.vatterger.engine.handler.gridmap.GridMap2DField;
import de.vatterger.engine.handler.gridmap.GridMap2D;
import de.vatterger.engine.handler.gridmap.GridMapFlag;
import de.vatterger.engine.handler.gridmap.GridMapQuery;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CollisionRadius;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.StaticObject;
import de.vatterger.game.systems.graphics.GraphicalProfilerSystem;

public class DynamicObjectMapSystem extends BaseEntitySystem {

	private static DynamicObjectMapSystem SELF;
	
	private ComponentMapper<AbsolutePosition>	apm;
	private ComponentMapper<CollisionRadius>	crm;
	
	private final IntBag insertedBag = new IntBag(1024);
	private final IntBag removedBag = new IntBag(1024);
	
	private final GridMap2D gridMap;

	private final CollisionRadius crDefault = new CollisionRadius(0f);
	
	private Profiler profiler = new Profiler("DynamicObjectMapSystem", TimeUnit.MICROSECONDS);
	
	public DynamicObjectMapSystem() {
		
		super(Aspect.all(AbsolutePosition.class, CullDistance.class).exclude(StaticObject.class));

		if(SELF != null) throw new IllegalStateException("More than one instance of Singleton StaticObjectMapSystem detected.");
		
		SELF = this;
		
		gridMap = new GridMap2DField(1000, 40, 25, 1, 0f, 0f);
		
		GraphicalProfilerSystem.registerProfiler("DynamicObjectMapSystem", Color.YELLOW, profiler);
	}
	
	@Override
	protected void inserted (int entityId) {
		//System.out.println("Insert-Request for " + entityId + " into DynamicObjectMapSystem.");
		insertedBag.add(entityId);
		removedBag.removeValue(entityId);
	}
	
	@Override
	protected void removed (int entityId) {
		//System.out.println("Remove-Request for " + entityId + " from DynamicObjectMapSystem.");
		removedBag.add(entityId);
		insertedBag.removeValue(entityId);
	}
	
	@Override
	protected void begin () {
		profiler.start();
	}
	
	@Override
	protected void processSystem () {
		
		synchronized (gridMap) {
		
			// Update newly inserted entities.
			if(insertedBag.size() > 0) {
				int[]	entityIds		= insertedBag.getData();
				int	entityIds_size	= insertedBag.size();
				
				for (int i = 0; i < entityIds_size; i++) {
		
					final int entityId = entityIds[i];
					
					if(gridMap.contains(entityId)) {
						continue;
					}
					
					AbsolutePosition ap = apm.get(entityId);
					CollisionRadius cr = crm.getSafe(entityId, crDefault);
					
					final int gf = cr.dst > 0 ? GridMapFlag.COLLISION : 0;
					
					if(!gridMap.put(entityId, ap.position.x + cr.offsetX, ap.position.y + cr.offsetY, cr.dst, gf)) {
						//System.err.println("Inserting " + entityId + " into Dynamic GridMap failed.");
						world.delete(entityId);
					} else {
						//System.out.println("Inserted " + entityId + " into Dynamic GridMap.");
					}
				}
				
				insertedBag.setSize(0);
			}

			// Update residential entities.
			int[] entityIds		= getEntityIds().getData();
			int entityIds_size	= getEntityIds().size();
			
			for (int i = 0; i < entityIds_size; i++) {
	
				final int entityId = entityIds[i];
				
				AbsolutePosition ap = apm.get(entityId);
				CollisionRadius cr = crm.getSafe(entityId, crDefault);
				
				
				if(!gridMap.update(entityId, ap.position.x + cr.offsetX, ap.position.y + cr.offsetY)) {
					//System.err.println("Updating " + entityId + " in Dynamic GridMap failed.");
					world.delete(entityId);
				} else {
					//System.out.println("Updated " + entityId + " in Dynamic GridMap.");
				}
			}
			
			// Update newly removed entities.
			if(removedBag.size() > 0) {
				
				entityIds		= removedBag.getData();
				entityIds_size	= removedBag.size();
				
				for (int i = 0; i < entityIds_size; i++) {
					
					final int entityId = entityIds[i];

					if(!gridMap.contains(entityId)) {
						continue;
					}
					
					if(!gridMap.remove(entityId)) {
						//System.err.println("Removing " + entityId + " from Dynamic GridMap failed.");
						world.delete(entityId);
					} else {
						//System.out.println("Removed " + entityId + " from Dynamic GridMap.");
					}
				}
				
				removedBag.setSize(0);
			}
		}
	}
	
	@Override
	protected void end () {
		profiler.stop();
	}

	public static void getData(float x1,float y1, float x2, float y2, int gf, GridMapQuery result) {
		
		synchronized (SELF.gridMap) {
			SELF.gridMap.get(x1, y1, x2, y2, gf, result);
		}
	}
}
