package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.Turret;

public class TurretRotateToMouseSystem extends IteratingSystem {

	private ComponentMapper<Attached> am;
	private ComponentMapper<Turret> tm;
	private ComponentMapper<AbsolutePosition> pm;
	private ComponentMapper<AbsoluteRotation> srm;
	
	private Vector3 v0 = new Vector3();
	private Vector3 v1 = new Vector3();
	private Vector3 v2 = new Vector3();
	
	@Wire(name="camera")
	private Camera camera;
	
	private Profiler profiler = new Profiler("SpriteRender");
	
	public TurretRotateToMouseSystem() {
		super(Aspect.all(AbsolutePosition.class, Turret.class, Attached.class).exclude(Culled.class));
		GraphicalProfilerSystem.registerProfiler("TurretLookAtMouse", Color.BROWN, profiler);
	}
	
	@Override
	protected void begin () {
		profiler.start();
	}
	
	@Override
	protected void end () {
		profiler.stop();
	}
	
	@Override
	protected void process(int e) {
		
		final Attached ac = am.get(e);
		final Turret tc = tm.get(e);
		
		if(ac.parentId < 0) return;
		
		v0.set(pm.get(e).position);
		Math2D.castMouseRay(v1, camera);
		
		v2.set(v1.sub(v0)).nor();
		
		float rot = MathUtils.atan2(v2.y, v2.x) * MathUtils.radDeg - 90f;
		rot = Math2D.normalize_360(rot - srm.get(ac.parentId).rotation);
		rot = Math2D.limitAngle(rot, tc.angleMin, tc.angleMax);
		
		ac.rotation = rot;
	}
}
