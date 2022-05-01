package de.vatterger.game.systems.gameplay;

import java.util.concurrent.TimeUnit;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.Color;

import de.vatterger.engine.handler.gridmap.GridMap2DField;
import de.vatterger.engine.handler.gridmap.GridMap2D;
import de.vatterger.engine.handler.gridmap.GridMap2DMultiResolution;
import de.vatterger.engine.handler.gridmap.GridMapFlag;
import de.vatterger.engine.handler.gridmap.GridMapQuery;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CollisionRadius;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.StaticObject;
import de.vatterger.game.systems.graphics.GraphicalProfilerSystem;

public class StaticObjectMapSystem extends BaseEntitySystem {

	private static StaticObjectMapSystem SELF;
	
	private ComponentMapper<AbsolutePosition>	apm;
	private ComponentMapper<CollisionRadius>	crm;
	
	private final GridMap2D gridMap;

	private final IntBag insertedBag = new IntBag(1024);
	private final IntBag removedBag = new IntBag(1024);
	
	private final CollisionRadius crDefault = new CollisionRadius(0f);
	
	private Profiler profiler = new Profiler("StaticObjectMapSystem", TimeUnit.MICROSECONDS);
	
	public StaticObjectMapSystem() {
		
		super(Aspect.all(AbsolutePosition.class, CullDistance.class, StaticObject.class));

		if(SELF != null) throw new IllegalStateException("More than one instance of Singleton StaticObjectMapSystem detected.");
		
		SELF = this;
		
		gridMap = new GridMap2DField(1000, 40, 25, 1, 0f, 0f);
		//gridMap = new GridMap2DMultiResolution(new int[]{5}, 1000, 40, 25, 2, 0f, 0f);
		
		GraphicalProfilerSystem.registerProfiler("StaticObjectMapSystem", Color.YELLOW, profiler);
	}
	
	@Override
	protected void inserted (int entityId) {
		//System.out.println("Insert-Request for " + entityId + " into StaticObjectMapSystem.");
		insertedBag.add(entityId);
		removedBag.removeValue(entityId);
	}
	
	@Override
	protected void removed (int entityId) {
		//System.out.println("Remove-Request for " + entityId + " from StaticObjectMapSystem.");
		removedBag.add(entityId);
		insertedBag.removeValue(entityId);
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
					//System.err.println("Adding " + entityId + " to Static GridMap failed.");
					world.delete(entityId);
				} else {
					//System.out.println("Added " + entityId + " to Static GridMap.");
				}

			}
			
			entityIds		= removedBag.getData();
			entityIds_size	= removedBag.size();
			
			for (int i = 0; i < entityIds_size; i++) {
				
				final int entityId = entityIds[i];

				if(!gridMap.contains(entityId)) {
					continue;
				}
				
				if(!gridMap.remove(entityId)) {
					//System.err.println("Removing " + entityId + " from Static GridMap failed.");
					world.delete(entityId);
				} else {
					//System.out.println("Removed " + entityId + " from Static GridMap.");
				}
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

	public static void getData(float x1,float y1, float x2, float y2, int gf, GridMapQuery result) {
		
		synchronized (SELF.gridMap) {
			SELF.gridMap.get(x1, y1, x2, y2, gf, result);
		}
	}
}
