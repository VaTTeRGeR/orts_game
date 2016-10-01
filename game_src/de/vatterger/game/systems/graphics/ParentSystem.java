package de.vatterger.game.systems.graphics;

import java.util.ArrayList;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.SpriteRotation;

public class ParentSystem extends BaseEntitySystem{

	private ComponentMapper<Position> pm;
	private ComponentMapper<SpriteRotation> srm;
	private ComponentMapper<Attached> am;

	private ArrayList<IntArray> levelIds = new ArrayList<IntArray>();
	
	private Vector3 v0 = new Vector3();
	
	public ParentSystem() {
		super(Aspect.all(Position.class, Attached.class));
	}
	
	@Override
	protected void inserted(int e) {
		Attached attached = am.get(e);
		if(am.has(attached.parentId)) {
			attached.level = am.get(attached.parentId).level + 1;
		} else {
			attached.level = 0;
		}
		IntArray a;

		levelIds.ensureCapacity(attached.level + 1);
		if(levelIds.size() < attached.level + 1 || levelIds.get(attached.level) == null) {
			levelIds.add(attached.level, a = new IntArray());
		} else {
			a = levelIds.get(attached.level);
		}
		
		a.add(e);
	}
	
	@Override
	protected void removed(int e) {
		Attached attached = am.get(e);
		IntArray a = levelIds.get(attached.level);
		a.removeIndex(a.indexOf(e));
	}
	
	@Override
	protected void processSystem() {
		for (IntArray intArray : levelIds) {
			for (int i = 0; i < intArray.size; i++) {
				solve(intArray.get(i));
			}
		}
	}
	
	protected void solve(int e) {
		Attached ac = am.get(e);
		if(world.getEntityManager().isActive(ac.parentId)) {
			Vector3 posChild = pm.get(e).position;
			Vector3 posParent = pm.get(ac.parentId).position;
			Vector3 offsetChild = ac.offset;

			v0.set(offsetChild);
			v0.rotate(Vector3.Z, Math2D.roundAngle(srm.get(ac.parentId).rotation, 16));
			v0.add(posParent);

			posChild.set(v0);
		} else {
			world.delete(e);
		}
	}
}
