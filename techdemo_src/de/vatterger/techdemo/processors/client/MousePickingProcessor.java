package de.vatterger.techdemo.processors.client;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.GameUtil;
import de.vatterger.techdemo.application.GameConstants;

@Wire
public class MousePickingProcessor extends BaseEntitySystem {

	private Camera camera;
	private ImmediateModeRenderer20 imr20;

	public MousePickingProcessor(Camera camera, ImmediateModeRenderer20 imr20) {
		super(Aspect.all());
		this.camera = camera;
		this.imr20 = imr20;
	}

	@Override
	protected void begin() {
		Vector3 v = GameUtil.intersectMouseGroundPlane(camera, Gdx.input.getX(), Gdx.input.getY());

		if (GameConstants.DEBUG_MOUSE_RAY_INTERSECTION) {

			imr20.begin(camera.combined, GL20.GL_LINES);

			Color red = Color.RED;

			GameUtil.line(v, v.cpy().add(0, 0, 3f), red, imr20);

			imr20.end();
		}
	}

	@Override
	protected void processSystem() {
		// TODO Auto-generated method stub
	}
}
