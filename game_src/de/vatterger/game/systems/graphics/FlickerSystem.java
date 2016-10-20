package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.unit.UnitHandler;
import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.Turret;

public class FlickerSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<AbsoluteRotation> arm;
	
	private float t_reset = 1f/(800f/60f);
	private float t_now = 0f;
	
	private Sound mgSound = Gdx.audio.newSound(Gdx.files.internal("assets/sound/mg34.wav"));
	private boolean playing = false;
	private Camera camera;
	
	private Vector3 v0 = new Vector3();
	private Vector3 v1 = new Vector3();

	public FlickerSystem(Camera camera) {
		super(Aspect.all(AbsolutePosition.class, AbsoluteRotation.class, Turret.class));
		this.camera = camera;
	}
	
	@Override
	protected void begin() {
		t_now += world.getDelta();
		if(playing && !Gdx.input.isTouched()) {
			mgSound.stop();
			playing = false;
		} else if(!playing && Gdx.input.isTouched()) {
			mgSound.loop(0.1f);
			playing = true;
		}
	}
	
	protected void process(int e) {
		boolean shoot = t_now >= t_reset && Gdx.input.isTouched();
		if(shoot) {
			v1.set(Vector3.Y).rotate(Vector3.Z, arm.get(e).rotation).scl(750f);
			UnitHandler.createTracer("7_92mg_tracer", apm.get(e).position, Math2D.castRayCam(v0, camera), v1, arm.get(e).rotation, world);
		}
	}
	
	@Override
	protected void end() {
		if(t_now >= t_reset) {
			t_now -= t_reset;
		}
	}
	
	@Override
	protected void dispose() {
		mgSound.dispose();
	}
}
