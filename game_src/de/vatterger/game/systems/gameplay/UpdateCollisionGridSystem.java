package de.vatterger.game.systems.gameplay;

import java.util.concurrent.TimeUnit;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Circle;

import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CollisionRadius;

public class UpdateCollisionGridSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<CollisionRadius> crm;
	
	static Circle c0 = new Circle();
	
	private static float offsetX = 0f;
	private static float offsetY = 0f;
	
	private static float cellSizeInv = 1f/2f;
	
	private Profiler profiler = new Profiler("UpdateCollisionGridSystem", TimeUnit.MICROSECONDS);
	
	public UpdateCollisionGridSystem() {
		super(Aspect.all(AbsolutePosition.class, CollisionRadius.class));
	}
	
	@Override
	protected void inserted(int entityId) {
		
		AbsolutePosition ap = apm.get(entityId);
		CollisionRadius cr = crm.get(entityId);
	}
	
	@Override
	protected void removed(int entityId) {
		
	}
	
	@Override
	protected void begin() {
		profiler.start();
	}
	
	@Override
	protected void process(int entityId) {
		
	}
	
	@Override
	protected void end() {
		profiler.log();
	}
	
	private static int cellX(float p) {
		p -= offsetX;
		return (int)(p > 0f ? p*cellSizeInv : 0f);
	}

	private static int cellY(float p) {
		p -= offsetY;
		return (int)(p > 0f ? p*cellSizeInv : 0f);
	}
}
