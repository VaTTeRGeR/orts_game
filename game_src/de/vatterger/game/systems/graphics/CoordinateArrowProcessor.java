package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;

public class CoordinateArrowProcessor extends IteratingSystem {

	private ImmediateModeRenderer20 immediateRenderer;
	private Camera camera;
	
	private boolean toggleRender = false;
	
	public CoordinateArrowProcessor(ImmediateModeRenderer20 immediateRenderer, Camera camera) {
		super(Aspect.all());
		this.immediateRenderer = immediateRenderer;
		this.camera = camera;
	}

	@Override
	protected void begin() {
		if(Gdx.input.isKeyJustPressed(Keys.F2))
			toggleRender = !toggleRender;
	}
	
	@Override
	protected void process(int e) {
		if(!toggleRender)
			return;
		
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
