package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Position;

public class MoveEntitySystem extends IteratingSystem {
	
	private Camera camera;

	private ComponentMapper<Position>		pm;
	private ComponentMapper<CullDistance>	cdm;

	private boolean clicked;

	private Vector3 v0 = new Vector3();
	private Vector3 v1 = new Vector3();
	
	public MoveEntitySystem(Camera camera) {
		super(Aspect.all(Position.class));
		this.camera = camera;
	}

	@Override
	protected void begin() {
		clicked = Gdx.input.isButtonPressed(Buttons.LEFT);
	}
	
	@Override
	protected void process(int e) {
		if(clicked && (!cdm.has(e) || cdm.get(e).visible)) {
			Vector3 pc = pm.get(e).position;
			v0.set(pc);
			
			Math2D.castRayCam(v1, camera);
			v1.sub(v0).nor().scl(world.delta*100f/3.6f);
			pc.add(v1);
		}
	}
}
