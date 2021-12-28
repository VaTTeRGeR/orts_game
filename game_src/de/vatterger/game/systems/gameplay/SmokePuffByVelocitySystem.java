package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.unit.UnitHandlerJSON;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.MoveCurve;
import de.vatterger.game.components.gameobject.Turrets;

public class SmokePuffByVelocitySystem extends IteratingSystem {
	
	private ComponentMapper<AbsolutePosition>	apm;
	private ComponentMapper<AbsoluteRotation>	arm;
	private ComponentMapper<MoveCurve>	 		mcm;
	
	public SmokePuffByVelocitySystem() {
		super(Aspect.all(AbsolutePosition.class, AbsoluteRotation.class, Turrets.class).exclude(Culled.class));
	}

	@Override
	protected void process(int e) {
		
		if(MathUtils.randomBoolean(mcm.has(e) ? 0.3f : 0.05f)) {
			
			Vector3 pos = apm.get(e).position;
			float rot = arm.get(e).rotation;
			
			UnitHandlerJSON.createAnimatedEffect("puff", pos, rot, false, world);
		}
	}
}
