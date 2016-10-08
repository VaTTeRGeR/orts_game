package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.unit.UnitHandler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.RemoveTimed;
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
		TracerTarget tc = tm.get(e);

		v0.set(pm.get(e).position);
		v1.set(tc.targetPos);
		
		if(v0.dst(v1) > tc.dist) {
			world.delete(e);
			v0.set(tc.targetPos).lerp(pm.get(e).position, 0.25f).add(MathUtils.random(-tc.spreadX, tc.spreadX), MathUtils.random(-tc.spreadY, tc.spreadY), 0f);
			int mud_decal = UnitHandler.createStaticObject("mud_decal", v0, SpriteLayer.GROUND1);
			world.edit(mud_decal).add(new RemoveTimed(1f));
		} else {
			tc.dist = v0.dst(v1);
		}
	}
}
