package de.vatterger.game.systems.graphics;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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

import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.systems.gameplay.MaintainCollisionMapSystem;

public class CollisionFieldRenderSystem extends BaseSystem {

	@Wire(name="camera")
	private Camera camera;
	
	private ShapeRenderer shapeRenderer;
	
	public CollisionFieldRenderSystem() {
		shapeRenderer = new ShapeRenderer(8192);
	}
	
	@Override
	protected void begin() {
		
		shapeRenderer.setProjectionMatrix(camera.combined/*.cpy().scl(1f, Metrics.ymodp, 1f)*/);
		shapeRenderer.setTransformMatrix(new Matrix4(new Vector3(0f, 0f, camera.position.y - 1024f),new Quaternion(Vector3.X, -45f), new Vector3(1f, 1f, 1f)));
		shapeRenderer.updateMatrices();
		
		shapeRenderer.begin(ShapeType.Filled);
	}
	
	private static final boolean overlaps(float x1,float y1, float r1, float x2,float y2, float r2) {
		
		float dx = x2 - x1;
		float dy = y2 - y1;
		
		float distance = dx * dx + dy * dy;
		
		float radiusSum = r1 + r2;
		
		return distance < radiusSum * radiusSum;
	}
	
	private final boolean isColliding(float x, float y, float r) {
		
		float[] data = MaintainCollisionMapSystem.getData(x - r, y - r, x + 2f*r, y + 2f*r);
		
		//System.out.println("point col with " + ((int)data[0]) + " circles.");
		
		int imax = ((int)data[0]) * 3;
		
		for (int i = 1; i < imax + 1;) {
			
			if(overlaps(x, y, r, data[i++], data[i++], data[i++])) {
				return true;
			}
		}
		
		return false;
	}

	private float circle(float y, float radius) {
		return (float)Math.sqrt(radius * radius - y * y);
	}
	
	final float step = 1f;
	final float x_max = 100f;
	final float y_max = 100f;
	
	private int index(float x, float y) {
		
		int x_div = (int)(x_max/step);
		int y_div = (int)(y_max/step);
		
		int x_index = (int)(x / step);
		
		if(x_index < 0 || x_index >= x_div) {
			return -1;
		}
		
		int y_index = (int)(y / step);
		
		if(y_index < 0 || y_index >= y_div) {
			return -1;
		}
		
		int index = x_index + x_div * y_index;
		
		return MathUtils.clamp(index, 0, x_div * y_div - 1);
	}
	
	byte[] map = new byte[(int)((x_max/step) * (y_max/step))];
	
	@Override
	protected void processSystem () {
		
		Profiler p_build = new Profiler("Build map", TimeUnit.MICROSECONDS);
		
		Vector3 mouseCoords = Math2D.castMouseRay(new Vector3(), camera);
		
		float base_x1 = (float)(int)(mouseCoords.x - x_max/2f);
		float base_y1 = (float)(int)(mouseCoords.y - y_max/2f);
		
		float base_x2 = base_x1 + x_max;
		float base_y2 = base_y1 + y_max;
		
		Profiler p_getData = new Profiler("Get Collision Data", TimeUnit.MICROSECONDS);
		
		float[] data = MaintainCollisionMapSystem.getData(base_x1, base_y1, base_x2, base_y2);
		
		p_getData.log();
		
		Arrays.fill(map, (byte)0);
		
		for (int i = 1; i < data[0]*3 + 1; i+=3) {
			
			float x = data[i];
			float y = data[i+1];
			
			float dx = x - base_x1;
			float dy = y - base_y1;
			
			float rc = data[i+2];
			
			// Scanline circle from -rc to +rc
			for (float yc = -rc; yc < rc; yc += step) {
				
				float xc = circle(yc, rc);
				
				int index_start	= index(dx - xc + step/2f, yc + dy);
				int index_end		= index(dx + xc + step/2f, yc + dy);
				
				if(index_start < 0 && index_end < 0)
					continue;
					
				if(yc + dy < 0f || yc + dy > y_max) {
					continue;
				}
				
				if(index_start < 0) {
					index_start = index(0f, yc + dy);
				}
				
				if(index_end < 0) {
					index_end = index(x_max - step, yc + dy);
				}
				
				for (int id = index_start; id <= index_end; id++) {
					map[id] = 1;
				}
			}
		}
		
		p_build.log();
		
		Profiler p_disp = new Profiler("Display map", TimeUnit.MICROSECONDS);
		
		for (float dx = 0; dx < x_max; dx += step) {
			for (float dy = 0; dy < y_max; dy += step) {
				
				if(map[index(dx, dy)] != 0)
					shapeRenderer.setColor(Color.RED);
				else
					shapeRenderer.setColor(Color.GREEN);
					
				shapeRenderer.rect(base_x1 + dx - step/2, base_y1 + dy + step/2, step, step);
				//shapeRenderer.circle(base_x1 + dx + step/2, base_y1 + dy + step/2, step/2, 8);
			}
		}
		
		p_disp.log();
	}

	/*@Override
	protected void processSystem () {
		
		final float step = 1f;
		final float x_max = 10f;
		final float y_max = 10f;
		
		Vector3 mouseCoords = Math2D.castMouseRay(new Vector3(), camera);
		
		final float base_x = mouseCoords.x;
		final float base_y = mouseCoords.y;
		
		for (float x = 0; x < x_max; x += step) {
			for (float y = 0; y < y_max; y += step) {
				
				float xc = base_x + x - x_max/2f;
				float yc = base_y + y - y_max/2f;
				
				if(isColliding(xc, yc, step / 2f * 1.41f))
					shapeRenderer.setColor(Color.RED);
				else
					shapeRenderer.setColor(Color.GREEN);
					
				shapeRenderer.rect(xc-step/2, yc-step/2, step, step);
				//shapeRenderer.circle(xc, yc, step / 2f, 8);
			}
		}
	}*/

	@Override
	protected void end() {
		shapeRenderer.end();
	}

	@Override
	protected void dispose() {
		shapeRenderer.dispose();
	}
}
