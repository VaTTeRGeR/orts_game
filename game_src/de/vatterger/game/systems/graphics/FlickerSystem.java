package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;

import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Flicker;

public class FlickerSystem extends IteratingSystem {

	private ComponentMapper<CullDistance> cdm;
	
	private float t_reset = 1f/(600/60f);
	private float t_off = 0.25f*t_reset;
	private float t_now = 0f;
	private float t_wait = 0f;
	
	public FlickerSystem() {
		super(Aspect.all(CullDistance.class, Flicker.class));
	}
	
	@Override
	protected void begin() {
		t_now += world.getDelta();
		if(t_now >= t_reset + t_wait) {
			t_now -= t_reset;
			if(MathUtils.randomBoolean(0.01f))
				t_wait = 1f;
			else
				t_wait = 0f;
		}
	}
	
	protected void process(int e) {
		CullDistance cd = cdm.get(e);
		cd.visible = t_now <= t_off;
	}
}
