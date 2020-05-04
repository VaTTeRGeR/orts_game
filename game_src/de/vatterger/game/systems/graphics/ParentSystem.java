package de.vatterger.game.systems.graphics;

import java.util.ArrayList;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.Culled;

public class ParentSystem extends BaseEntitySystem{

	private ComponentMapper<Attached> am;
	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<AbsoluteRotation> arm;
	
	private final int MAX_LEVEL = 7;

	private ArrayList<IntArray> levelIds = new ArrayList<IntArray>(MAX_LEVEL + 1);
	
	private AbsoluteRotation rotationDefault = new AbsoluteRotation(0f);
	
	public ParentSystem() {
		super(Aspect.all(AbsolutePosition.class, Attached.class).exclude(Culled.class));
	}
	
	@Override
	protected void initialize() {
		
		// Bucket size decreases exponentially to save memory
		int bucketsize = 16 * 1024;
		
		for (int i = 0; i < MAX_LEVEL + 1; i++) {
			
			levelIds.add(new IntArray(bucketsize));
			
			bucketsize = Math.max(1, bucketsize / 4);
		}
	}
	
	@Override
	protected void inserted(int entityId) {
		
		Attached attached = am.get(entityId);
		
		if(attached.parentId == -1) {
			
			world.delete(entityId);
			
		} else {

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
	}
	
	@Override
	protected void removed(int entityId) {
		
		Attached attached = am.get(entityId);
		
		IntArray a = levelIds.get(attached.level);
		a.removeIndex(a.indexOf(entityId));
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
		AbsoluteRotation ar = arm.getSafe(e, rotationDefault);
		
		if(ac.parentId == -1) {
			
			world.delete(e);
			
		} else {
			
			float tempRotation = arm.getSafe(ac.parentId, rotationDefault ).rotation;
			
			Vector3 posChild	= apm.get(e).position;
			Vector3 posParent	= apm.get(ac.parentId).position;
			Vector3 offsetChild	= ac.offset;

			posChild.set(offsetChild);
			posChild.rotate(Vector3.Z, tempRotation);
			posChild.add(posParent);

			tempRotation += ac.rotation;
			
			ar.rotation = Math2D.roundAngle(tempRotation % 360f, 16);
		}
	}
}
