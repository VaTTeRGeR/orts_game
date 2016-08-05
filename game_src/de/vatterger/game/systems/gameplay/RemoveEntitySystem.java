package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.GameUtil;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Terrain;

public class RemoveEntitySystem extends IteratingSystem {
	
	private Camera camera;

	private ComponentMapper<Position> pm;

	private int best;
	private float bestDist;
	private boolean clicked;

	private Vector3 v0 = new Vector3();
	
	@SuppressWarnings("unchecked")
	public RemoveEntitySystem(Camera camera) {
		super(Aspect.all(Position.class).exclude(Terrain.class));
		this.camera = camera;
	}

	@Override
	protected void begin() {
		clicked = Gdx.input.isButtonPressed(Buttons.RIGHT);

		if(clicked){
			best = -1;
			bestDist = Float.MAX_VALUE;
		}
	}
	
	@Override
	protected void process(int e) {
		if(clicked) {
			float eDist = GameUtil.intersectMouseGroundPlane(camera, Gdx.input.getX(), Gdx.input.getY()).dst(v0.set(pm.get(e).v));
			if(eDist < 4f && (best == -1 || eDist < bestDist)) {
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
