package de.vatterger.game.systems.graphics;

import java.util.ArrayList;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.RelativePosition;
import de.vatterger.game.components.gameobject.RelativeRotation;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;

public class ParentSystem extends BaseEntitySystem{

	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<AbsoluteRotation> arm;
	private ComponentMapper<RelativePosition> rpm;
	private ComponentMapper<RelativeRotation> rrm;
	private ComponentMapper<Attached> am;

	private ArrayList<IntArray> levelIds = new ArrayList<IntArray>();
	
	private Vector3 v0 = new Vector3();
	
	public ParentSystem() {
		super(Aspect.all(AbsolutePosition.class, AbsoluteRotation.class, Attached.class));
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
	
	private void solve(int e) {
		Attached ac = am.get(e);
		if(world.getEntityManager().isActive(ac.parentId)) {
			Vector3 posChild = apm.get(e).position;
			Vector3 posParent = apm.get(ac.parentId).position;
			Vector3 offsetChild = ac.offset;

			v0.set(offsetChild);
			if(rpm.has(e)) {
				v0.add(rpm.get(e).position);
			}
			v0.rotate(Vector3.Z, Math2D.roundAngle(arm.get(ac.parentId).rotation, 16));
			v0.add(posParent);

			arm.get(e).rotation = arm.get(ac.parentId).rotation + ac.rotation ;
			if(rrm.has(e)) {
				arm.get(e).rotation += rrm.get(e).rotation;
			}
			arm.get(e).rotation %= 360f;
			
			posChild.set(v0);
		} else {
			world.delete(e);
		}
	}
}
