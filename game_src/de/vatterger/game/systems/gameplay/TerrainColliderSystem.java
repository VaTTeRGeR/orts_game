package de.vatterger.game.systems.gameplay;

import java.util.HashMap;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CollisionRadius;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.TerrainHeightField;

public class TerrainColliderSystem extends BaseEntitySystem{

	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<TerrainHeightField> thfm;

	private HashMap<Integer, IntArray> colliderMap = new HashMap<>(64);
	
	public TerrainColliderSystem() {
		super(Aspect.all(AbsolutePosition.class,TerrainHeightField.class));
	}
	
	@Override
	protected void inserted(int entityId) {
		
		Vector3 position = apm.get(entityId).position;
		TerrainHeightField thf = thfm.get(entityId);
		
		float[][] hf = thf.height;
		
		Vector3 tempPosition = new Vector3();
		
		IntArray colliders = new IntArray(false,hf.length * hf[0].length);
		
		for (int i = 0; i < hf.length; i++) {
			for (int j = 0; j < hf[0].length; j++) {

				if(hf[i][j] < 0.5f) {
					
					tempPosition.set(position).add(thf.grid_size * j, thf.grid_size * (hf.length - 1 - i), 0f);
					
					int collider = world.create();

					world.edit(collider)
					.add(new AbsolutePosition(tempPosition))
					.add(new CullDistance(thf.grid_size * 0.35f))
					.add(new CollisionRadius(thf.grid_size * 0.35f));
					
					colliders.add(collider);
				}
			}
		}
		
		colliderMap.put(entityId,colliders);
	}
	
	@Override
	protected void removed(int entityId) {
		
		IntArray colliders = colliderMap.remove(entityId);
		
		if(colliders != null) {
			
			for (int i = 0; i < colliders.size; i++) {
				world.delete(colliders.get(i));
			}
		}
	}

	@Override
	protected void processSystem() {}
}
