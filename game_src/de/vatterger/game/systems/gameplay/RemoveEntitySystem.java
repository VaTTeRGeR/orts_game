package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.gridmap.GridMapQuery;
import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.Culled;

public class RemoveEntitySystem extends BaseEntitySystem {
	
	private static final float DELETE_RADIUS = 5f;
	
	@Wire(name="camera")
	private Camera camera;

	private Vector3 v0 = new Vector3();
	private Vector3 v1 = new Vector3();

	GridMapQuery query = new GridMapQuery(64, true, true);
	
	public RemoveEntitySystem() {
		super(Aspect.all(AbsolutePosition.class).exclude(Culled.class));
	}

	@Override
	protected void processSystem () {
		
		if(!Gdx.input.isButtonPressed(Buttons.LEFT)){
			return;
		}
		
		Math2D.castMouseRay(v0, camera);
		
		query.clear();
		
		//Profiler p = new Profiler("GET", TimeUnit.NANOSECONDS);
		
		StaticObjectMapSystem.getData(v0.x-DELETE_RADIUS, v0.y-DELETE_RADIUS, v0.x+DELETE_RADIUS, v0.y+DELETE_RADIUS, 0, query);
		DynamicObjectMapSystem.getData(v0.x-DELETE_RADIUS, v0.y-DELETE_RADIUS, v0.x+DELETE_RADIUS, v0.y+DELETE_RADIUS, 0, query);
		
		//p.log();
		
		final int size = query.getSize();
		final int[] idData = query.getIdData();
		final float[] colData = query.getCollisionData();
		
		for (int i = 0; i < size; i++) {
			
			v1.set(colData[3*i+0], colData[3*i+1], 0f);
			
			final float dst = v1.dst(v0);
			
			if(dst <= DELETE_RADIUS) {
				world.delete(idData[i]);
			}
		}
	}
}
