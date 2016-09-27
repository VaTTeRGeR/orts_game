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
	
	private float t_reset = 1f/(550f/60f);
	private float t_now = 0f;
	
	public ShootSystem() {
		super(Aspect.all(CullDistance.class, Flicker.class));
	}
	
	protected void process(int e) {
		t_now += world.getDelta();
		if(t_now >= t_reset) {
			t_now -= t_reset;
			UnitHandler.createTracer("7_92mg_tracer", pm.get(e).position.cpy().add(MathUtils.random(-0.5f, 0.5f), MathUtils.random(2f)+2, 0), Vector3.Y.cpy().rotate(Vector3.Z, srm.get(e).rotation).scl(850f), srm.get(e).rotation);
			UnitHandler.createTracer("7_92mg_tracer", pm.get(e).position.cpy().add(MathUtils.random(-0.5f, 0.5f), MathUtils.random(2f)+2, 0), Vector3.Y.cpy().rotate(Vector3.Z, srm.get(e).rotation).scl(850f), srm.get(e).rotation);
		}
	}
}
