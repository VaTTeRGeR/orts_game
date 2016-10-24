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
		
		if(Gdx.input.isKeyPressed(Keys.G)) {
			UnitHandler.createGroundTile("tile", Math2D.castRayCam(v0, camera), world);
		}
		
		v1.set(Gdx.input.getDeltaX(), -Gdx.input.getDeltaY());
		if(!v1.isZero(2f)) {
			angle = (v1.angle() - 90) % 360f;
			lastAngle = angle;
		} else {
			angle = lastAngle;
		}
		
		if(Gdx.input.isKeyPressed(Keys.F)) {
			UnitHandler.createTracer("7_92mg_tracer", Math2D.castRayCam(v0, camera), new Vector3(0,1,0).rotate(Vector3.Z, angle).scl(MathUtils.random(45f, 55f)).add(Math2D.castRayCam(v0, camera)), new Vector3(0, 80f, 0).rotate(Vector3.Z, angle), angle, world);
		}
	}
}