package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.SpriteRotation;

public class ParentSystem extends IteratingSystem{

	private ComponentMapper<Position> pm;
	private ComponentMapper<SpriteRotation> srm;
	private ComponentMapper<Attached> am;

	private Vector3 v0 = new Vector3();
	
	public ParentSystem() {
		super(Aspect.all(Position.class, Attached.class));
	}
	
	@Override
	protected void inserted(int e) {
		Attached attached = am.get(e);
		if(am.has(attached.parentId)) {
			throw new IllegalStateException("Parent cannot be parented. max depth is 1!");
		}
	}
	
	protected void process(int e) {
		Attached ac = am.get(e);
		if(world.getEntityManager().isActive(ac.parentId)) {
			Vector3 posChild = pm.get(e).position;
			Vector3 posParent = pm.get(ac.parentId).position;
			Vector3 offsetChild = ac.offset;

			v0.set(offsetChild);
			v0.rotate(Vector3.Z, Math2D.roundAngleEight(srm.get(ac.parentId).rotation));
			v0.add(posParent);

			posChild.set(v0);
		} else {
			world.delete(e);
		}
	}
}
