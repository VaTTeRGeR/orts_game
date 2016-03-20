package de.vatterger.entitysystem.processors.client;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

@Wire
public class DrawGLDebugProcessor extends EntityProcessingSystem {
	
	private SpriteBatch batch = new SpriteBatch();
	private BitmapFont bmf = new BitmapFont();
	private Matrix4 mat = new Matrix4();
	private Camera cam;
	private float t = 0f;
	
	public DrawGLDebugProcessor(Camera cam) {
		super(Aspect.getEmpty());
		this.cam = cam;
	}
	
	@Override
	protected void initialize() {
		GLProfiler.enable();
		bmf.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

	}

	@Override
	protected void begin() {
		t += world.getDelta();
		t = t%(360f/10f);
		mat.idt().translate(Vector3.Zero).scl(0.05f).rotate(Vector3.X, 90f).rotate(Vector3.Y, 10f*t).mulLeft(cam.combined);
		batch.setProjectionMatrix(mat);
		batch.begin();
		bmf.draw(batch, "TEXTURE BINDS:"+GLProfiler.textureBindings, 5,15*1);
		bmf.draw(batch, "DRAW CALLS:"+GLProfiler.drawCalls, 5,15*2);
		bmf.draw(batch, "SHADER SWITCHES:"+GLProfiler.shaderSwitches, 5,15*3);
		bmf.draw(batch, "VERTEX COUNT:"+GLProfiler.vertexCount.total, 5,15*4);
		batch.end();
		GLProfiler.reset();
	}
	
	@Override
	protected void process(Entity e) {}
	
	@Override
	protected void dispose() {
		GLProfiler.disable();
		batch.dispose();
	}
}
