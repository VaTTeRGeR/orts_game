package de.vatterger.game.systems.gameplay;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.unit.UnitBuilder;
import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.MoveCurve;
import de.vatterger.game.components.gameobject.MovementParameters;
import de.vatterger.game.components.gameobject.Velocity;

public class CreateTestEntitySystem extends BaseSystem {
	
	@Wire(name="camera")
	private Camera camera;

	private Vector3 v0 = new Vector3();
	private Vector2 v1 = new Vector2();
	private float angle = 0f;
	private float lastAngle = 0f;
	
	@Override
	protected void processSystem() {

		if(Gdx.input.isKeyPressed(Keys.O)) {
			new UnitBuilder("soldier").spawnUnit(Math2D.castMouseRay(v0, camera), world);
		}
		
		if(Gdx.input.isKeyPressed(Keys.P)) {
			UnitBuilder builder = new UnitBuilder("m4a1");
			for (int k = 0; k < 1; k++) {
				
				int i = builder.spawnUnit(Math2D.castMouseRay(v0, camera), world);
				
				Vector3[] pathPoints = new Vector3[5];
				pathPoints[0] = v0.cpy();
				
				for (int j = 1; j < 5; j++) {
					float randX = MathUtils.randomTriangular(-150f, 150f);
					float randY = MathUtils.randomTriangular(-150f, 150f);
					pathPoints[j] = pathPoints[j-1].cpy().add(randX, randY, 0f);
				}
				
				world.edit(i).add(new MoveCurve(pathPoints, new MovementParameters()));
			}
		}
		
		if(Gdx.input.isKeyPressed(Keys.P) && (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))) {
			new UnitBuilder("pz1b").spawnUnit(Math2D.castMouseRay(v0, camera).add(MathUtils.random(-10f, 10f), MathUtils.random(-10f, 10f), 0f), world);
		}

		if(Gdx.input.isKeyJustPressed(Keys.B)) {
			int entityId = new UnitBuilder("mg_bunker").spawnUnit(Math2D.castMouseRay(v0, camera), world);
			world.edit(entityId).add(new AbsoluteRotation(MathUtils.random(360f)));
		}
		
		if(Gdx.input.isKeyPressed(Keys.T)) {
			int entityId = new UnitBuilder("pine-dense").spawnUnit(Math2D.castMouseRay(v0, camera).add(MathUtils.randomTriangular(-10f, 10f), MathUtils.randomTriangular(-10f, 10f), 0f), world);
			world.edit(entityId).add(new AbsoluteRotation(MathUtils.random(360f)));
		}
		
		if(Gdx.input.isKeyPressed(Keys.F) && Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
			
			int numEffects = 10;
			float spread = 100f;
			
			for (int i = 0; i < numEffects; i++) {
				new UnitBuilder("flash_big").spawnUnit(Math2D.castMouseRay(v0, camera).add(MathUtils.randomTriangular(-spread, spread), MathUtils.randomTriangular(-spread, spread), 0f), world);
			}
		}
		
		v1.set(-Gdx.input.getDeltaX(), Gdx.input.getDeltaY());
		if(!v1.isZero(2f)) {
			angle = Math2D.normalize_360(v1.angleDeg() + 90f);
			lastAngle = angle;
		} else {
			angle = lastAngle;
		}
	}
}
