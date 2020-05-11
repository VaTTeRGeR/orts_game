package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.CullingParent;
import de.vatterger.game.components.gameobject.NotCulled;

import java.util.concurrent.TimeUnit;

public class CullingSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition>	pm;
	private ComponentMapper<Culled>				cm;
	private ComponentMapper<CullDistance>		cdm;
	
	@Wire(name="camera")
	private Camera camera;

	private final Rectangle r0 = new Rectangle();
	private final Rectangle r1 = new Rectangle();

	private Profiler profiler = new Profiler("CullingSystem", TimeUnit.MICROSECONDS);
	
	public CullingSystem() {
		
		super(Aspect.all(AbsolutePosition.class, CullDistance.class, NotCulled.class).exclude(CullingParent.class));
		
		GraphicalProfilerSystem.registerProfiler("CullingSystem", Color.CORAL.cpy().add(0f, 0.2f, 0f, 0f), profiler);
	}
	
	@Override
	protected void begin() {

		profiler.start();
		
		r0.setSize(camera.viewportWidth, camera.viewportHeight);
		r0.setCenter(camera.position.x, camera.position.y);
	}
	
	protected void process(int entityId) {
		
		final Vector3			pos	= pm.get(entityId).position;
		final CullDistance	cd		= cdm.get(entityId);
		
		r1.setSize(cd.dst * 2f, cd.dst * 2f * Metrics.ymodp);
		r1.setCenter(pos.x + cd.offsetX, (pos.y + cd.offsetY) * Metrics.ymodp);
		
		cd.visible = r0.overlaps(r1);
		
		if(!cd.visible) {
			//System.out.println("Removed NotCulled from " + entityId);
			world.edit(entityId).add(Culled.flyweight).remove(NotCulled.flyweight);
		} else if(cm.has(entityId)) {
			cm.remove(entityId);
		}
	}
	
	@Override
	protected void end() {
		profiler.stop();
	}
}
