package de.vatterger.game.systems.gameplay;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.unit.UnitHandler;
import de.vatterger.engine.util.Math2D;

public class CreateTestEntitySystem extends BaseSystem {
	
	private Camera camera;

	private Vector3 v0 = new Vector3();
	private Vector2 v1 = new Vector2();
	private float angle = 0f;
	private float lastAngle = 0f;
	
	public CreateTestEntitySystem(Camera camera) {
		this.camera = camera;
	}
	
	@Override
	protected void processSystem() {
		if(Gdx.input.isKeyPressed(Keys.O)) {
			UnitHandler.createInfatry("soldier", Math2D.castRayCam(v0, camera), world);
		}

		if(Gdx.input.isKeyJustPressed(Keys.P)) {
			UnitHandler.createTank("pz1b", Math2D.castRayCam(v0, camera), world);
		}
		
		if(Gdx.input.isKeyPressed(Keys.P) && (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))) {
			UnitHandler.createTank("pz1b", Math2D.castRayCam(v0, camera).add(MathUtils.random(-10f, 10f), MathUtils.random(-10f, 10f), 0f), world);
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.G)) {
			UnitHandler.createGroundTile("tile_grass", Math2D.castRayCam(v0, camera), world);
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.J)) {
			UnitHandler.createRandomTerrainTile(Math2D.castRayCam(v0, camera), world);
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.H)) {
			UnitHandler.createGroundTile("tile_dirt", Math2D.castRayCam(v0, camera), world);
		}
		
		v1.set(-Gdx.input.getDeltaX(), Gdx.input.getDeltaY());
		if(!v1.isZero(2f)) {
			angle = Math2D.normalize_360(v1.angle() + 90f);
			lastAngle = angle;
		} else {
			angle = lastAngle;
		}
		
		if(Gdx.input.isKeyPressed(Keys.F) && MathUtils.randomBoolean(1f)) {
			UnitHandler.createTracer("flash_small", Math2D.castRayCam(v0, camera), new Vector3(0,1,0).rotate(Vector3.Z, angle).scl(MathUtils.random(75f, 100f)).add(Math2D.castRayCam(v0, camera)), new Vector3(0, MathUtils.random(200f,500f), 0).rotate(Vector3.Z, angle), world);
		}
	}
}
