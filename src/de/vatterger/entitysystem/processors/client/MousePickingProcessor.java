package de.vatterger.entitysystem.processors.client;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.util.GameUtil;

@Wire
public class MousePickingProcessor extends EntityProcessingSystem {

	private Camera camera;
	private Input input;
	private ImmediateModeRenderer20 imr20;

	public MousePickingProcessor(Camera camera, Input input, ImmediateModeRenderer20 imr20) {
		super(Aspect.getEmpty());
		this.camera = camera;
		this.input = input;
		this.imr20 = imr20;
	}

	@Override
	protected void begin() {
		Vector3 v = GameUtil.intersectMouseGroundPlane(camera, input.getX(), input.getY());

		if (GameConstants.DEBUG_MOUSE_RAY_INTERSECTION) {

			imr20.begin(camera.combined, GL20.GL_LINES);

			Color red = Color.RED;

			GameUtil.line(v, v.cpy().add(0, 0, 3f), red, imr20);

			imr20.end();
		}
	}

	@Override
	protected void process(Entity e) {}
}
