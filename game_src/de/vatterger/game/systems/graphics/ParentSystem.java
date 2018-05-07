package de.vatterger.game.systems.graphics;

import java.util.ArrayList;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;

public class ParentSystem extends BaseEntitySystem{

	private ComponentMapper<Attached> am;
	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<AbsoluteRotation> arm;

	private ArrayList<IntArray> levelIds = new ArrayList<IntArray>();
	
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
		
		System.out.println("Attached Level: " + attached.level);
		
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
		for (int i = 0; i < levelIds.size(); i++) {
			IntArray intArray = levelIds.get(i);
			for (int j = 0; j < intArray.size; j++) {
				solve(intArray.get(j));
			}
		}
	}
	
	private void solve(int e) {
		Attached ac = am.get(e);
		AbsoluteRotation ar = arm.get(e);
		
		if(world.getEntityManager().isActive(ac.parentId)) {
			AbsoluteRotation ar_parent = arm.get(ac.parentId);
			
			Vector3 posChild	= apm.get(e).position;
			Vector3 posParent	= apm.get(ac.parentId).position;
			Vector3 offsetChild	= ac.offset;

			posChild.set(offsetChild);
			posChild.rotate(Vector3.Z, ar_parent.rotation);
			posChild.add(posParent);

			ar.rotation = ar_parent.rotation + ac.rotation;
			ar.rotation %= 360f;
		} else {
			world.delete(e);
		}
	}
}
