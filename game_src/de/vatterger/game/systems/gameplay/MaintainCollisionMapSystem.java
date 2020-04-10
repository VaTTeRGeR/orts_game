package de.vatterger.game.systems.gameplay;

import java.util.concurrent.TimeUnit;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.engine.handler.gridmap.GridMap2D;
import de.vatterger.engine.handler.gridmap.GridMapUtil;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CollisionRadius;
import de.vatterger.game.components.gameobject.Culled;

public class MaintainCollisionMapSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<CollisionRadius> crm;
	
	private GridMap2D gridMap = new GridMap2D(10, 0, 0);
	
	private IntBag fillBag = new IntBag();
	private float[] data = new float[32];
	
	private Profiler profiler = new Profiler("MaintainCollisionMapSystem", TimeUnit.MICROSECONDS);
	
	private static MaintainCollisionMapSystem SELF;
	
	public MaintainCollisionMapSystem() {
		super(Aspect.all(AbsolutePosition.class, CollisionRadius.class).exclude(Culled.class));
		SELF = this;
	}
	
	@Override
	protected void begin() {
		
		profiler.start();
		
		gridMap.clear();		
		
		//System.out.println("Cleared GridMap.");
		//System.out.println();
	}

	@Override
	protected void process(int entityId) {
		
		AbsolutePosition ap = apm.get(entityId);
		CollisionRadius cr = crm.get(entityId);
		
		gridMap.insertCircle(ap.position.x + cr.offsetX, ap.position.y + cr.offsetY, cr.dst, entityId, GridMapUtil.COLLISION);
	}
	
	public static float[] getData(float x1,float y1, float x2, float y2) {
		
		final IntBag fillBag = SELF.fillBag;
		float[] data = SELF.data;
		
		fillBag.clear();
		
		SELF.gridMap.getEntities(GridMapUtil.COLLISION, new Rectangle(x1, y1, x2-x1, y2-y1), fillBag);
		
		final int dataSize = fillBag.size() * 3 + 1;
		
		if(dataSize > data.length) {
			SELF.data = (data = new float[dataSize]);
		}
		
		int i_data = 0;
		int i_entity = 0;
		
		data[i_data++] = fillBag.size();
		
		while(i_data < dataSize) {
			
			AbsolutePosition ap = SELF.apm.get(fillBag.get(i_entity));
			CollisionRadius cr = SELF.crm.get(fillBag.get(i_entity++));
			
			data[i_data++] = ap.position.x + cr.offsetX;
			data[i_data++] = ap.position.y + cr.offsetY;
			data[i_data++] = cr.dst;
		}
		
		return data;
	}
}
