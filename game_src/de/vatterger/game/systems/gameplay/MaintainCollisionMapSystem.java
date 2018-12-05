package de.vatterger.game.systems.gameplay;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Circle;

import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CollisionRadius;

public class MaintainCollisionMapSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<CollisionRadius> crm;
	
	static Circle c0 = new Circle();
	
	static int size_new;
	static float[]	data_new;
	static AtomicReference<float[]>	data_current = new AtomicReference<>();
	
	private Profiler profiler = new Profiler("MaintainCollisionMapSystem", TimeUnit.MICROSECONDS);
	
	static {
		data_current.set(new float[] {0});
	}
	
	public MaintainCollisionMapSystem() {
		super(Aspect.all(AbsolutePosition.class, CollisionRadius.class));
	}
	
	@Override
	protected void begin() {
		
		profiler.start();
		
		size_new = 1;
		data_new = new float[getEntityIds().size() * 3 + 1];
		
	}

	@Override
	protected void process(int entityId) {
		
		AbsolutePosition ap = apm.get(entityId);
		CollisionRadius cr = crm.get(entityId);
		
		data_new[size_new++] = ap.position.x + cr.offsetX;
		data_new[size_new++] = ap.position.y + cr.offsetY;
		data_new[size_new++] = cr.dst;
		
	}
	
	@Override
	protected void end() {
		
		//profiler.log();
		
		data_new[0] = (float)((size_new - 1) / 3);
		data_current.set(data_new);
		
	}
	
	public static float[] getData() {
		return data_current.get();
	}
}
