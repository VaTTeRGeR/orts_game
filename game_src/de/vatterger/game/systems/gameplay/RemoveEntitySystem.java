package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Metrics;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Position;

public class RemoveEntitySystem extends IteratingSystem {
	
	private Camera camera;

	private ComponentMapper<Position>		pm;
	private ComponentMapper<CullDistance>	cdm;

	private int best;
	private float bestDist;
	private boolean clicked;

	private Vector3 v0 = new Vector3();
	private Vector3 v1 = new Vector3();
	
	public RemoveEntitySystem(Camera camera) {
		super(Aspect.all(Position.class));
		this.camera = camera;
	}

	@Override
	protected void begin() {
		clicked = Gdx.input.isButtonPressed(Buttons.LEFT) && Gdx.input.justTouched();

		if(clicked){
			best = -1;
			bestDist = Float.MAX_VALUE;
		}
	}
	
	@Override
	protected void process(int e) {
		if(clicked && (!cdm.has(e) || cdm.get(e).visible)) {
			float eDist = camera.unproject(v1.set(Gdx.input.getX(), Gdx.input.getY(), 0f)).scl(1f, Metrics.ymodu, 0f).dst(v0.set(pm.get(e).position));
			if(eDist < 4f && eDist <= bestDist) {
				best = e;
				bestDist = eDist;
			}
		}
	}
	
	@Override
	protected void end() {
		if(clicked){
			if(best != -1) {
				world.delete(best);
			}
		}
	}

}
