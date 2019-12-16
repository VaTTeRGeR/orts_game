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

import java.util.concurrent.TimeUnit;

public class CullingSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition>	pm;
	private ComponentMapper<CullDistance>		cdm;
	private ComponentMapper<Culled>				cm;
	
	@Wire(name="camera")
	private Camera camera;

	private Rectangle r0 = new Rectangle();
	private Rectangle r1 = new Rectangle();

	private Profiler profiler = new Profiler("CullingSystem", TimeUnit.MICROSECONDS);
	
	@SuppressWarnings("unchecked")
	public CullingSystem() {
		
		super(Aspect.all(AbsolutePosition.class, CullDistance.class).exclude(CullingParent.class));
		
		GraphicalProfilerSystem.registerProfiler("Culling", Color.CORAL, profiler);
	}
	
	@Override
	protected void begin() {

		profiler.start();
		
		r0.setSize(camera.viewportWidth, camera.viewportHeight);
		r0.setCenter(camera.position.x, camera.position.y);
	}
	
	protected void process(int entityId) {
		
		final Vector3		pos	= pm.get(entityId).position;
		final CullDistance	cd	= cdm.get(entityId);
		
		r1.setSize(cd.dst * 2f, cd.dst * 2f * Metrics.ymodp);
		r1.setCenter(pos.x + cd.offsetX, (pos.y + cd.offsetY) * Metrics.ymodp);
		
		cd.visible = r0.overlaps(r1);
		
		boolean hasCulled = cm.has(entityId);
		
		if(!cd.visible && !hasCulled) {
			world.edit(entityId).add(Culled.flyweight);
		} else if(cd.visible && hasCulled) {
			world.edit(entityId).remove(Culled.flyweight);
		}
	}
	
	@Override
	protected void end() {
		profiler.stop();
	}
}
