package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.unit.UnitHandlerJSON;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.MoveCurve;
import de.vatterger.game.components.gameobject.Turrets;

public class SmokePuffByVelocitySystem extends IteratingSystem {
	
	private ComponentMapper<AbsolutePosition>	pm;
	private ComponentMapper<MoveCurve>	 		mcm;
	
	public SmokePuffByVelocitySystem() {
		super(Aspect.all(AbsolutePosition.class, Turrets.class));
	}

	@Override
	protected void process(int e) {

		Vector3 pos = pm.get(e).position;
		MoveCurve mc = mcm.getSafe(e,null);
		
		if(MathUtils.randomBoolean(mc != null ? 0.25f : 0.05f)) {
			UnitHandlerJSON.createAnimatedEffect("puff", pos, world);
		}
	}
}
