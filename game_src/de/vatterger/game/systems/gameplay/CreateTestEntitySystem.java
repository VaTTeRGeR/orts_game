package de.vatterger.game.systems.gameplay;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import de.vatterger.engine.handler.unit.UnitHandlerJSON;
import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.MoveCurve;
import de.vatterger.game.components.gameobject.MovementParameters;

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
			UnitHandlerJSON.createInfatry("soldier", Math2D.castMouseRay(v0, camera), world);
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.P)) {
			for (int k = 0; k < 1; k++) {
				int i = UnitHandlerJSON.createTank("m4a1", Math2D.castMouseRay(v0, camera), world);
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
			UnitHandlerJSON.createTank("pz1b", Math2D.castMouseRay(v0, camera).add(MathUtils.random(-10f, 10f), MathUtils.random(-10f, 10f), 0f), world);
		}

		if(Gdx.input.isKeyJustPressed(Keys.B)) {
			int entityId = UnitHandlerJSON.createStaticObject("mg_bunker", Math2D.castMouseRay(v0, camera), world);
			world.edit(entityId).add(new AbsoluteRotation(MathUtils.random(360f)));
		}
		
		if(Gdx.input.isKeyPressed(Keys.T)) {
			UnitHandlerJSON.createStaticObject("tree0" + MathUtils.random(1, 4), Math2D.castMouseRay(v0, camera).add(MathUtils.randomTriangular(-10f, 10f), MathUtils.randomTriangular(-10f, 10f), 0f), world);
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.T) && Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
			
			float sizef = 1500f;
			
			for (int i = 0; i < 10; i++) {
				UnitHandlerJSON.createStaticObject("tree03", new Vector3(MathUtils.random(0f, sizef), MathUtils.random(0f, sizef), 0f), world);
			}
			
			for (int i = 0; i < 100; i++) {
				UnitHandlerJSON.createStaticObject("tree01", new Vector3(MathUtils.random(0f, sizef), MathUtils.random(0f, sizef), 0f), world);
				UnitHandlerJSON.createStaticObject("tree02", new Vector3(MathUtils.random(0f, sizef), MathUtils.random(0f, sizef), 0f), world);
				UnitHandlerJSON.createStaticObject("tree04", new Vector3(MathUtils.random(0f, sizef), MathUtils.random(0f, sizef), 0f), world);
			}
		}
		
		if(Gdx.input.isKeyPressed(Keys.F) && Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
			
			int numEffects = 10;
			float spread = 100f;
			
			for (int i = 0; i < numEffects; i++) {
				UnitHandlerJSON.createAnimatedEffect("flash_big", Math2D.castMouseRay(v0, camera).add(MathUtils.randomTriangular(-spread, spread), MathUtils.randomTriangular(-spread, spread), 0f), 0f, true, world);
			}
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.J)) {
			UnitHandlerJSON.createRandomTerrainTile(Math2D.castMouseRay(v0, camera), world);
		}
		
		v1.set(-Gdx.input.getDeltaX(), Gdx.input.getDeltaY());
		if(!v1.isZero(2f)) {
			angle = Math2D.normalize_360(v1.angle() + 90f);
			lastAngle = angle;
		} else {
			angle = lastAngle;
		}
		
		if(Gdx.input.isKeyPressed(Keys.F) &! Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) && MathUtils.randomBoolean(1f)) {
			Math2D.castMouseRay(v0, camera);
			UnitHandlerJSON.createTracer(MathUtils.randomBoolean() ? "flash_big" : "flash_small", v0.cpy(), new Vector3(0,1,0).rotate(Vector3.Z, angle).scl(MathUtils.random(75f, 100f)).add(v0.add(0f, 0f, MathUtils.random(0f, 2f))), new Vector3(0, MathUtils.random(200f,500f), 0).rotate(Vector3.Z, angle), world);
		}
	}
}
