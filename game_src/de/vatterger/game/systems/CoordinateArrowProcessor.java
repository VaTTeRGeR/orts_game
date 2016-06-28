package de.vatterger.game.systems;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;

public class CoordinateArrowProcessor extends IteratingSystem {

	private ImmediateModeRenderer20 immediateRenderer;
	private Camera camera;
	
	public CoordinateArrowProcessor(ImmediateModeRenderer20 immediateRenderer, Camera camera) {
		super(Aspect.all());
		this.immediateRenderer = immediateRenderer;
		this.camera = camera;
	}

	@Override
	protected void process(int e) {
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
		immediateRenderer.vertex(0,10, 0);
		immediateRenderer.end();

		immediateRenderer.begin(camera.combined, GL20.GL_LINES);
		immediateRenderer.color(Color.BLUE);
		immediateRenderer.vertex(0, 0, 0);
		immediateRenderer.color(Color.BLUE);
		immediateRenderer.vertex(0, 0, 10);
		immediateRenderer.end();
	}
}
