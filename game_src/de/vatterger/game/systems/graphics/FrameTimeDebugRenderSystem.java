package de.vatterger.game.systems.graphics;

import java.util.concurrent.TimeUnit;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;

import de.vatterger.engine.util.Profiler;

public class FrameTimeDebugRenderSystem extends BaseSystem {

	private OrthographicCamera camera;
	private ShapeRenderer shapeRenderer;

	private int values_target_size;
	private int bar_size = 4;
	
	private Vector3 v0 = new Vector3();
	private Vector3 v1 = new Vector3();
	private Queue<Float> q0 = new Queue<Float>();
	private Queue<Float> q1 = new Queue<Float>();
	private Color c = new Color();
	private Profiler profiler;
	
	public FrameTimeDebugRenderSystem(Profiler profiler) {
		this.camera = new OrthographicCamera();
		shapeRenderer = new ShapeRenderer(4096);
		this.profiler = profiler;
	}
	
	private int pressCount = 0;
	private boolean show = false;
	private boolean hold = false;

	@Override
	protected void processSystem() {
		if(Gdx.input.isKeyPressed(Keys.F2)) {
			pressCount++;
		} else if(pressCount <= 10 && pressCount > 0) {
			show = !show;
			pressCount = -10;
		} else {
			pressCount = 0;
		}

		if(pressCount > 1 && show) {
			hold = true;
		} else {
			hold = false;
		}
		
		if(hold && Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
			bar_size = Math.min(bar_size*2, 16);
		} else if(hold && Gdx.input.isKeyJustPressed(Keys.LEFT)) {
			bar_size = Math.max(bar_size/2, 1);
		}
		
		if(!hold) {
			for (int i = 0; i < bar_size; i++) {
				values_target_size = Gdx.graphics.getWidth();
				while (q0.size >= values_target_size)
					q0.removeFirst();
				q0.addLast(world.getDelta());
				while (q1.size >= values_target_size)
					q1.removeFirst();
				q1.addLast(((float)TimeUnit.NANOSECONDS.toMicros(profiler.getTimeElapsed()))/1000f);
			}
		}

		if (show) {
			camera.viewportWidth = Gdx.graphics.getWidth();
			camera.viewportHeight = Gdx.graphics.getHeight();
			camera.position.set(Gdx.graphics.getWidth()/2,-1f,Gdx.graphics.getHeight()/2);
			camera.lookAt(Gdx.graphics.getWidth()/2,0f,Gdx.graphics.getHeight()/2);
			camera.update();

			shapeRenderer.setProjectionMatrix(camera.combined);
			shapeRenderer.begin(ShapeType.Line);

			shapeRenderer.setColor(Color.WHITE);

			v0.set(0f, 0f, 0f);
			v1.set(values_target_size, 0f, 0f);
			shapeRenderer.line(v0, v1);
			
			v0.set(0f, 0f, 60f);
			v1.set(values_target_size, 0f, 60f);
			shapeRenderer.line(v0, v1);

			float min = Float.MAX_VALUE, max = Float.MIN_VALUE, avg = 0f;

			float dx = 0f;
			for (Float value : q0) {
				
				value = 1f/value;
				
				float lerpValue = value/60f;

				c.set(Color.RED);
				c.lerp(Color.GREEN, lerpValue);
				c.a = 1f;

				shapeRenderer.setColor(c);
				
				v0.set(dx, 0f, 0f);
				v1.set(v0.x, v0.y, value);

				shapeRenderer.line(v0, v1);
				
				min = Math.min(min, v1.z);
				max = Math.max(max, v1.z);
				avg += v1.z;

				dx += 1f;
			}
			avg /= q0.size;

			float z = 120f;
			float m = 4f;

			shapeRenderer.setColor(Color.RED);
			v0.set(0f, 0f, z);
			v1.set(values_target_size, 0f, z);
			shapeRenderer.line(v0, v1);
			
			shapeRenderer.setColor(Color.RED);
			v0.set(0f, 0f, z + m*16f);
			v1.set(values_target_size, 0f, z + m*16f);
			shapeRenderer.line(v0, v1);
			
			shapeRenderer.setColor(Color.RED);
			v0.set(0f, 0f, z + m*4f);
			v1.set(values_target_size, 0f, z + m*4f);
			shapeRenderer.line(v0, v1);
			
			shapeRenderer.setColor(Color.RED);
			v0.set(0f, 0f, z + m*8f);
			v1.set(values_target_size, 0f, z + m*8f);
			shapeRenderer.line(v0, v1);
			
			shapeRenderer.setColor(Color.RED);
			v0.set(0f, 0f, z + m*12f);
			v1.set(values_target_size, 0f, z + m*12f);
			shapeRenderer.line(v0, v1);
			
			dx = 0f;
			for (Float value : q1) {
				
				float lerpValue = (value)/(1000f/60f);

				c.set(Color.GREEN);
				c.lerp(Color.RED, lerpValue);
				c.a = 1f;

				shapeRenderer.setColor(c);
				
				v0.set(dx, 0f, z);
				v1.set(v0.x, v0.y, z + m*value);

				shapeRenderer.line(v0, v1);

				dx += 1f;
			}
			
			shapeRenderer.setColor(Color.YELLOW);
			v0.set(0f, 0f, min);
			v1.set(values_target_size, 0f, min);
			shapeRenderer.line(v0, v1);

			v0.set(0f, 0f, max);
			v1.set(values_target_size, 0f, max);
			shapeRenderer.line(v0, v1);

			shapeRenderer.setColor(Color.BLUE);
			v0.set(0f, 0f, avg);
			v1.set(values_target_size, 0f, avg);
			shapeRenderer.line(v0, v1);
			
			shapeRenderer.end();
		}
	}
	@Override
	protected void dispose() {
		shapeRenderer.dispose();
	}
}
