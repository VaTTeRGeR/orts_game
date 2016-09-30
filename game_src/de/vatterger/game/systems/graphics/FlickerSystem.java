package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;

import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.Flicker;

public class FlickerSystem extends IteratingSystem {

	private ComponentMapper<CullDistance> cdm;
	
	private float t_reset = 1f/(550/60f);
	private float t_off = 0.25f*t_reset;
	private float t_now = 0f;
	
	@SuppressWarnings("unchecked")
	public FlickerSystem() {
		super(Aspect.all(CullDistance.class, Flicker.class).exclude(Culled.class));
	}
	
	@Override
	protected void begin() {
		t_now += world.getDelta();
		if(t_now >= t_reset) {
			t_now -= t_reset;
		}
	}
	
	protected void process(int e) {
		CullDistance cd = cdm.get(e);
		cd.visible = t_now <= t_off && Gdx.input.isTouched();
	}
}
