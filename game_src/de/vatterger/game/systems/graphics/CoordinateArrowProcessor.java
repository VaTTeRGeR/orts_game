package de.vatterger.game.systems.graphics;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;

public class CoordinateArrowProcessor extends BaseSystem {

	private ImmediateModeRenderer20 immediateRenderer;
	private Camera camera;
	
	private boolean toggleRender = false;
	
	public CoordinateArrowProcessor(ImmediateModeRenderer20 immediateRenderer, Camera camera) {
		this.immediateRenderer = immediateRenderer;
		this.camera = camera;
	}
	
	@Override
	protected void processSystem() {
		if(Gdx.input.isKeyJustPressed(Keys.F1))
			toggleRender = !toggleRender;

		if (toggleRender) {
			immediateRenderer.begin(camera.combined, GL20.GL_LINES);
			immediateRenderer.color(Color.RED);
			immediateRenderer.vertex(0, 0, 0);
			immediateRenderer.color(Color.RED);
			immediateRenderer.vertex(10, 0, 0);
			immediateRenderer.end();

			immediateRenderer.begin(camera.combined, GL20.GL_LINES);
			immediateRenderer.color(Color.GREEN);
			immediateRenderer.vertex(0, 0, 0);
			immediateRenderer.color(Color.GREEN);
			immediateRenderer.vertex(0, 10, 0);
			immediateRenderer.end();

			immediateRenderer.begin(camera.combined, GL20.GL_LINES);
			immediateRenderer.color(Color.BLUE);
			immediateRenderer.vertex(0, 0, 0);
			immediateRenderer.color(Color.BLUE);
			immediateRenderer.vertex(0, 0, 10);
			immediateRenderer.end();
		}
	}
}
