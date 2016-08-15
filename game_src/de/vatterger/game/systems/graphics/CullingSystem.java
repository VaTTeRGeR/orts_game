package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Position;

public class CullingSystem extends IteratingSystem {

	private Camera		camera;

	private ComponentMapper<Position> pm;
	private ComponentMapper<CullDistance> cdm;
	
	private Vector3 v0 = new Vector3();

	public CullingSystem(Camera camera) {
		super(Aspect.all(Position.class, CullDistance.class));
		this.camera = camera;
	}
	
	protected void process(int e) {
		cdm.get(e).visible = camera.frustum.sphereInFrustum(v0.set(pm.get(e).v), cdm.get(e).dst);
	}
}
