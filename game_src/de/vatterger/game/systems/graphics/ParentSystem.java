package de.vatterger.game.systems.graphics;

import java.util.ArrayList;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;

public class ParentSystem extends BaseEntitySystem{

	private ComponentMapper<Attached> am;
	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<AbsoluteRotation> arm;
	
	private final int MAX_LEVEL = 7;

	private ArrayList<IntArray> levelIds = new ArrayList<IntArray>(MAX_LEVEL+1);
	
	@SuppressWarnings("unchecked")
	public ParentSystem() {
		super(Aspect.all(AbsolutePosition.class, AbsoluteRotation.class, Attached.class).exclude(Culled.class));
	}
	
	@Override
	protected void initialize() {
		for (int i = 0; i < MAX_LEVEL+1; i++) {
			levelIds.add(new IntArray(64*1024));
		}
	}
	
	@Override
	protected void inserted(int entityId) {
		
		Attached attached = am.get(entityId);
		
		if(am.has(attached.parentId)) {
			attached.level = am.get(attached.parentId).level + 1;
		} else {
			attached.level = 0;
		}
		
		if(attached.level > MAX_LEVEL) {
			attached.level = MAX_LEVEL;
		}

		levelIds.get(attached.level).add(entityId);
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
			
			final int[] array = levelIds.get(i).items;
			final int size = levelIds.get(i).size;
			
			for (int j = 0; j < size; j++) {
				solve(array[j]);
			}
		}
	}
	
	private void solve(int e) {
		
		Attached ac = am.get(e);
		AbsoluteRotation ar = arm.get(e);
		
		if(world.getEntityManager().isActive(ac.parentId)) {
			
			AbsoluteRotation ar_parent = arm.getSafe(ac.parentId, new AbsoluteRotation(0));
			
			Vector3 posChild	= apm.get(e).position;
			Vector3 posParent	= apm.get(ac.parentId).position;
			Vector3 offsetChild	= ac.offset;

			posChild.set(offsetChild);
			posChild.rotate(Vector3.Z, ar_parent.rotation);
			posChild.add(posParent);

			ar.rotation = ar_parent.rotation + ac.rotation;
			ar.rotation %= 360f;
			ar.rotation = Math2D.roundAngle(ar.rotation, 16);
			
		} else {
			
			world.delete(e);
		}
	}
}
