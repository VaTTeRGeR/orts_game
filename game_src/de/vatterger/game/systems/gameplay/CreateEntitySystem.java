package de.vatterger.game.systems.gameplay;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.unit.UnitHandler;
import de.vatterger.engine.util.Metrics;

public class CreateEntitySystem extends BaseSystem {
	
	private Camera camera;

	private Vector3 v0 = new Vector3();
	
	public CreateEntitySystem(Camera camera) {
		this.camera = camera;
	}
	
	@Override
	protected void processSystem() {
		if(Gdx.input.isKeyPressed(Keys.O)) {
			UnitHandler.createInfatry("soldier", camera.unproject(v0.set(Gdx.input.getX(), Gdx.input.getY(), 0f)).scl(1f, Metrics.ymodu, 0f));
		}

		if(Gdx.input.isKeyJustPressed(Keys.P)) {
			UnitHandler.createTank("pz1b", camera.unproject(v0.set(Gdx.input.getX(), Gdx.input.getY(), 0f)).scl(1f, Metrics.ymodu, 0f));
		}
		
		if(Gdx.input.isKeyPressed(Keys.G)) {
			UnitHandler.createGroundTile("tile", camera.unproject(v0.set(Gdx.input.getX(), Gdx.input.getY(), 0f)).scl(1f, Metrics.ymodu, 0f));
		}

		if(Gdx.input.isKeyPressed(Keys.F)) {
			UnitHandler.createFlash("7_92mg_flash", camera.unproject(v0.set(Gdx.input.getX(), Gdx.input.getY(), 0f)).scl(1f, Metrics.ymodu, 0f), MathUtils.random(360f));
		}
	}
}
