package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.unit.UnitHandler;
import de.vatterger.engine.util.Metrics;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.SpriteLayer;
import de.vatterger.game.components.gameobject.TracerTarget;

public class TracerHitSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition> pm;
	private ComponentMapper<TracerTarget> tm;
	
	private Vector3 v0 = new Vector3();
	private Vector3 v1 = new Vector3();
	
	public TracerHitSystem() {
		super(Aspect.all(AbsolutePosition.class, TracerTarget.class));
	}
	
	@Override
	protected void inserted(int e) {
		v0.set(pm.get(e).position);
		v1.set(tm.get(e).targetPos);
		tm.get(e).dist = v0.dst(v1);
	}
	
	@Override
	protected void process(int e) {
		v0.set(pm.get(e).position);
		v1.set(tm.get(e).targetPos);
		if(v0.dst(v1) > tm.get(e).dist) {
			world.delete(e);
			float spread = 2f;
			int decalId = UnitHandler.createStaticObject("mud_decal", v0.set(tm.get(e).targetPos).add(MathUtils.random(-spread, spread), MathUtils.random(-spread, spread), 0f));
			world.edit(decalId).add(new SpriteLayer(SpriteLayer.VEGETATION_LOW));
		} else {
			tm.get(e).dist = v0.dst(v1);
		}
	}
}
