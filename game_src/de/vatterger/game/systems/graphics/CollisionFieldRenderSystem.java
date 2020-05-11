package de.vatterger.game.systems.graphics;

import java.util.Arrays;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.gridmap.GridMapFlag;
import de.vatterger.engine.handler.gridmap.GridMapQuery;
import de.vatterger.engine.util.Math2D;
import de.vatterger.game.systems.gameplay.DynamicObjectMapSystem;
import de.vatterger.game.systems.gameplay.StaticObjectMapSystem;

public class CollisionFieldRenderSystem extends BaseSystem {

	@Wire(name="camera")
	private Camera camera;
	
	private ShapeRenderer shapeRenderer;
	
	private GridMapQuery result = new GridMapQuery(512, false, true);
	
	public CollisionFieldRenderSystem() {
		shapeRenderer = new ShapeRenderer(8192);
	}
	
	@Override
	protected void begin() {
		
		shapeRenderer.setAutoShapeType(true);
		shapeRenderer.setProjectionMatrix(camera.combined/*.cpy().scl(1f, Metrics.ymodp, 1f)*/);
		shapeRenderer.setTransformMatrix(new Matrix4(new Vector3(0f, 0f, camera.position.y - 1024f), new Quaternion(Vector3.X, -45f), new Vector3(1f, 1f, 1f)));
		shapeRenderer.updateMatrices();
		
		shapeRenderer.begin(ShapeType.Filled);
	}
	
	private float circle(float y, float radius) {
		return (float)Math.sqrt(radius * radius - y * y);
	}
	
	private int index(float x, float y) {
		
		int x_div = (int)(x_max/step);
		int y_div = (int)(y_max/step);
		
		int x_index = MathUtils.clamp((int)(x/step), 0, x_div - 1);
		
		int y_index = MathUtils.clamp((int)(y/step), 0, y_div - 1);
		
		int index = x_index + x_div * y_index;
		
		return MathUtils.clamp(index, 0, x_div * y_div - 1);
	}
	
	final float step = 1f;
	final float x_max = 12f;
	final float y_max = 12f;
	
	byte[] map = new byte[(int)((x_max / step + 2f * step) * (y_max / step + 2f * step))];
	
	@Override
	protected void processSystem () {
		
		//Profiler p_build = new Profiler("Build map", TimeUnit.MICROSECONDS);
		
		Vector3 mouseCoords = Math2D.castMouseRay(new Vector3(), camera);
		
		// We extend by step amount in every direction since the border contains invalid (clamped from out of bounds) data.
		float base_x1 = step * (float) (int) ((mouseCoords.x - x_max/2f - step) / step);
		float base_y1 = step * (float) (int) ((mouseCoords.y - y_max/2f - step) / step);
		float base_x2 = base_x1 + x_max + step;
		float base_y2 = base_y1 + y_max + step;
		
		//Profiler p_getData = new Profiler("Get Collision Data", TimeUnit.MICROSECONDS);
		
		result.clear();
		
		StaticObjectMapSystem.getData(base_x1, base_y1, base_x2, base_y2, GridMapFlag.COLLISION, result);
		DynamicObjectMapSystem.getData(base_x1, base_y1, base_x2, base_y2, GridMapFlag.COLLISION, result);
		
		//p_getData.log();
		
		Arrays.fill(map, (byte)0);
		
		final float[] data = result.getCollisionData();
		final int size = result.getSize();
		
		for (int i = 0; i < size * 3; i += 3) {
			
			final float x = data[i];
			final float y = data[i+1];
			final float r = data[i+2];

			final float dx = x - base_x1 + step/2f;
			final float dy = y - base_y1;
			
			// Scanline circle from -rc to +rc
			for (float yc = -r; yc < r; yc += step) {
				
				final float xc = circle(yc, r);
				
				final int index_start	= index(dx - xc, yc + dy);
				final int index_end		= index(dx + xc, yc + dy);
				
				for (int id = index_start; id <= index_end; id++) {
					map[id] = 1;
				}
			}
		}
		
		//p_build.log();
		
		//Profiler p_disp = new Profiler("Display map", TimeUnit.MICROSECONDS);
		
		
		for (float dy = step; dy < y_max - step; dy += step) {
			for (float dx = step; dx < x_max - step; dx += step) {
				
				float dx_base = dx;
				
				int index_start = index(dx, dy);
				int index_end = index_start + 1;
				
				int runLength = 1;
				
				while(dx < x_max - 2f *step && map[index_start] == map[index_end]) {
					runLength++;
					index_end++;
					dx += step;
				}
				
				if(map[index_start] != 0)
					shapeRenderer.setColor(Color.RED);
				else
					shapeRenderer.setColor(Color.GREEN);
					
				shapeRenderer.rect(base_x1 + dx_base - step/2, base_y1 + dy + step/2, step * runLength, step);
			}
		}
		
		shapeRenderer.set(ShapeType.Line);
		shapeRenderer.setColor(Color.WHITE);
		
		for (int i = 0; i < size * 3; i += 3) {
			
			final float x = data[i];
			final float y = data[i+1];
			final float r = data[i+2];
			
			shapeRenderer.circle(x, y, r, 8);
		}
		
		/*for (int x = 0; x < 2000; x += 25) {
			shapeRenderer.line(x, 0, x, 2000);
		}
		
		for (int y = 0; y < 2000; y += 25) {
			shapeRenderer.line(0, y, 2000, y);
		}
		
		shapeRenderer.setColor(Color.PURPLE);
		
		for (int x = 0; x < 2000; x += 500) {
			shapeRenderer.line(x, 0, x, 2000);
		}
		
		for (int y = 0; y < 2000; y += 500) {
			shapeRenderer.line(0, y, 2000, y);
		}*/
		
		//p_disp.log();
	}

	@Override
	protected void end() {
		shapeRenderer.end();
	}

	@Override
	protected void dispose() {
		shapeRenderer.dispose();
	}
}
