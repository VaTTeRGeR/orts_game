package de.vatterger.game.systems.graphics;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;

public class FrameTimeDebugRenderSystem extends BaseSystem {

	private OrthographicCamera camera;
	private ShapeRenderer shapeRenderer;

	private int values_target_size;
	private int bar_size = 4;
	
	private Vector3 v0 = new Vector3();
	private Vector3 v1 = new Vector3();
	private Queue<Float> q = new Queue<Float>();
	private Color c = new Color();
	
	public FrameTimeDebugRenderSystem() {
		this.camera = new OrthographicCamera();
		shapeRenderer = new ShapeRenderer(4096);
	}
	
	private int pressCount = 0;
	private boolean show = false;
	private boolean hold = false;

	@Override
	protected void processSystem() {
		if(Gdx.input.isKeyPressed(Keys.F2)) {
			pressCount++;
		} else {
			if(pressCount <= 10 && pressCount > 0) {
				show = !show;
				pressCount = -10;
			} else {
				pressCount = 0;
			}
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
				while (q.size >= values_target_size)
					q.removeFirst();
				q.addLast(world.getDelta());
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
			v0.set(0f, 0f, 60f);
			v1.set(values_target_size, 0f, 60f);
			shapeRenderer.line(v0, v1);

			v0.set(0f, 0f, 120f);
			v1.set(values_target_size, 0f, 120f);
			shapeRenderer.line(v0, v1);
			
			float min = Float.MAX_VALUE, max = Float.MIN_VALUE, avg = 0f;

			float dx = 0f;
			for (Float value : q) {
				
				float lerpValue = (1f/60f)/(value);

				c.set(Color.RED);
				c.lerp(Color.GREEN, lerpValue);
				c.a = 1f;

				shapeRenderer.setColor(c);
				
				v0.set(dx, 0f, 0f);
				v1.set(v0.x, v0.y, 1f/value);

				shapeRenderer.line(v0, v1);
				
				min = Math.min(min, v1.z);
				max = Math.max(max, v1.z);
				avg += v1.z;

				dx += 1f;
			}
			avg /= q.size;
			
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
