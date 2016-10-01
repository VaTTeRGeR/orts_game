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
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.Flicker;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.SpriteRotation;

public class FlickerSystem extends IteratingSystem {

	private ComponentMapper<CullDistance> cdm;
	ComponentMapper<Position> pm;
	ComponentMapper<SpriteRotation> srm;
	
	private float t_reset = 1f/(550/60f);
	private float t_off = 0.25f*t_reset;
	private float t_now = 0f;
	
	private Sound mgSound = Gdx.audio.newSound(Gdx.files.internal("assets/sound/mg34.wav"));
	private boolean playing = false;
	private Camera camera;

	@SuppressWarnings("unchecked")
	public FlickerSystem(Camera camera) {
		super(Aspect.all(CullDistance.class, Flicker.class).exclude(Culled.class));
		this.camera = camera;
	}
	
	@Override
	protected void begin() {
		t_now += world.getDelta();
		if(t_now >= t_reset) {
			t_now -= t_reset;
		}
		if(playing && !Gdx.input.isTouched()) {
			mgSound.stop();
			playing = false;
		} else if(!playing && Gdx.input.isTouched()) {
			mgSound.loop(0.15f);
			playing = true;
		}
	}
	
	protected void process(int e) {
		CullDistance cd = cdm.get(e);
		cd.visible = t_now <= t_off && Gdx.input.isTouched();
		if(cd.visible) {
			Vector3 targetVec = new Vector3();
			Math2D.castRayCam(targetVec, camera);
			Vector3 vel = Vector3.Y.cpy().rotate(Vector3.Z, srm.get(e).rotation).scl(450f);
			//UnitHandler.createTracer("7_92mg_tracer", pm.get(e).position, targetVec, vel, srm.get(e).rotation);
		}
	}
	
	@Override
	protected void dispose() {
		mgSound.dispose();
	}
}
