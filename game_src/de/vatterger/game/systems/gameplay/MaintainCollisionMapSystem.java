package de.vatterger.game.systems.gameplay;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.Color;

import de.vatterger.engine.handler.gridmap.GridMapOptimized2D;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CollisionRadius;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.systems.graphics.GraphicalProfilerSystem;

public class MaintainCollisionMapSystem extends IteratingSystem {

	private static MaintainCollisionMapSystem SELF;
	
	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<CollisionRadius> crm;
	
	private static AtomicReference<GridMapOptimized2D> updatingHandle = new AtomicReference<>();
	private static AtomicReference<GridMapOptimized2D> readyHandle = new AtomicReference<>();

	private static final Object swapLock = new Object();
	
	private Profiler profiler = new Profiler("MaintainCollisionMapSystem", TimeUnit.MICROSECONDS);
	
	public MaintainCollisionMapSystem() {
		
		super(Aspect.all(AbsolutePosition.class, CollisionRadius.class).exclude(Culled.class));

		if(SELF != null) throw new IllegalStateException("More than one instance of Singleton MaintainCollisionMapSystem detected.");
		
		SELF = this;
		
		updatingHandle.set(new GridMapOptimized2D(150, 10, 4, true));
		readyHandle.set(new GridMapOptimized2D(150, 10, 4, true));
		
		GraphicalProfilerSystem.registerProfiler("MaintainCollisionMapSystem", Color.YELLOW, profiler);
	}
	
	@Override
	protected void begin() {
		
		profiler.start();
		
		updatingHandle.get().clear();		
	}
	
	@Override
	protected void process(int entityId) {
		
		AbsolutePosition ap = apm.get(entityId);
		CollisionRadius cr = crm.get(entityId);
		
		updatingHandle.get().put(entityId, ap.position.x + cr.offsetX, ap.position.y + cr.offsetY, cr.dst);
	}
	
	@Override
	protected void end () {
		
		final GridMapOptimized2D updated = updatingHandle.get();
		final GridMapOptimized2D ready = readyHandle.get();

		synchronized (swapLock) {
			readyHandle.set(updated);
		}
		
		updatingHandle.set(ready);
		
		profiler.stop();
	}

	public static float[] getData(float x1,float y1, float x2, float y2) {
		
		final IntBag fillBag = new IntBag(512);
		
		synchronized (swapLock) {
			readyHandle.get().getIdOnly(x1, y1, x2, y2, fillBag);
		}
	
		final int dataSize = fillBag.size() * 3 + 1;
		
		final float[] data = new float[dataSize];
		
		int i_data = 0;
		int i_entity = 0;
		
		data[i_data++] = fillBag.size();
		
		while(i_data < dataSize) {
			
			AbsolutePosition ap = SELF.apm.get(fillBag.get(i_entity));
			CollisionRadius cr = SELF.crm.get(fillBag.get(i_entity));
			
			data[i_data++] = ap.position.x + cr.offsetX;
			data[i_data++] = ap.position.y + cr.offsetY;
			data[i_data++] = cr.dst;
			
			i_entity++;
		}
		
		return data;
	}
}
