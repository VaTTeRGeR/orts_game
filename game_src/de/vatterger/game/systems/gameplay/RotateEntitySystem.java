package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.SpriteRotation;

public class RotateEntitySystem extends IteratingSystem {
	
	private ComponentMapper<SpriteRotation>		srm;
	private ComponentMapper<Attached>		am;
	
	public RotateEntitySystem() {
		super(Aspect.all(SpriteRotation.class));
	}

	@Override
	protected void inserted(int e) {
		SpriteRotation sr = srm.get(e);
		float angle = 360 * MathUtils.random();
		sr.rotation = angle;
		if(am.has(e) && srm.has(am.get(e).parentId)) {
			sr.rotation = srm.get(am.get(e).parentId).rotation+Math2D.roundAngleEight(angle);
		}
	}
	
	@Override
	protected void process(int e) {
		SpriteRotation sr = srm.get(e);
		sr.rotation = (sr.rotation + 45 * world.delta) % 360f;
	}
}
