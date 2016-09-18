package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Metrics;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Position;

public class CullingSystem extends IteratingSystem {

	private Camera		camera;

	private ComponentMapper<Position> pm;
	private ComponentMapper<CullDistance> cdm;
	
	private Rectangle r0 = new Rectangle();
	private Rectangle r1 = new Rectangle();

	public CullingSystem(Camera camera) {
		super(Aspect.all(Position.class, CullDistance.class));
		this.camera = camera;
	}
	
	protected void process(int e) {
		Vector3 pos = pm.get(e).position;
		CullDistance cd = cdm.get(e);
		
		r0.setSize(camera.viewportWidth, camera.viewportHeight);
		r0.setCenter(camera.position.x, camera.position.y);
		
		r1.setSize(cd.dst, cd.dst / Metrics.ymodp);
		r1.setCenter(pos.x, pos.y * Metrics.ymodp);
		
		cd.visible = r0.overlaps(r1);
	}
}
