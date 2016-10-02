package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.Turret;

public class TurretRotateToMouseSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition> pm;
	private ComponentMapper<AbsoluteRotation> srm;
	
	private Vector3 v0 = new Vector3();
	private Vector3 v1 = new Vector3();
	private Vector3 v2 = new Vector3();
	
	private Camera camera;
	
	public TurretRotateToMouseSystem(Camera camera) {
		super(Aspect.all(AbsolutePosition.class, AbsoluteRotation.class, Turret.class));
		this.camera = camera;
	}
	
	@Override
	protected void process(int e) {
		v0.set(pm.get(e).position);
		Math2D.castRayCam(v1, camera);
		v2.set(v1.sub(v0)).nor();
		float rot = MathUtils.atan2(v2.y, v2.x)*MathUtils.radDeg - 90;
		rot %= 360f;
		if(rot < 0)
			rot += 360f;
		srm.get(e).rotation = rot;
	}
}
