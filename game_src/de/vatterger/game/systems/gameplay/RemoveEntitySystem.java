package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.AbsolutePosition;

public class RemoveEntitySystem extends IteratingSystem {
	
	@Wire(name="camera")
	private Camera camera;

	private ComponentMapper<AbsolutePosition>		pm;
	private ComponentMapper<CullDistance>	cdm;

	private int best;
	private float bestDist;
	private boolean clicked;

	private Vector3 v0 = new Vector3();
	private Vector3 v1 = new Vector3();
	
	public RemoveEntitySystem() {
		super(Aspect.all(AbsolutePosition.class));
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
			float eDist = Math2D.castMouseRay(v1, camera).dst(v0.set(pm.get(e).position));
			if(eDist < 5f && eDist <= bestDist) {
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
