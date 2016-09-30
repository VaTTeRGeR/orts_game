package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.unit.UnitHandler;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Flicker;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.SpriteRotation;

public class ShootSystem extends IteratingSystem {

	ComponentMapper<Position> pm;
	ComponentMapper<SpriteRotation> srm;
	
	private float t_reset = 1f/(550/60f);
	private float t_now = 0f;
	
	public ShootSystem() {
		super(Aspect.all(CullDistance.class, Flicker.class));
	}
	
	@Override
	protected void begin() {
		t_now += world.getDelta();
	}
	
	protected void process(int e) {
		if(t_now >= t_reset) {
			Vector3 vel = Vector3.Y.cpy().rotate(Vector3.Z, srm.get(e).rotation).scl(850f);
			
			UnitHandler.createTracer("7_92mg_tracer", pm.get(e).position.cpy(), vel, srm.get(e).rotation);
			UnitHandler.createTracer("7_92mg_tracer", pm.get(e).position.cpy(), vel, srm.get(e).rotation);
		}
	}
	
	@Override
	protected void end() {
		if(t_now >= t_reset) {
			t_now -= t_reset;
		}
	}
}
