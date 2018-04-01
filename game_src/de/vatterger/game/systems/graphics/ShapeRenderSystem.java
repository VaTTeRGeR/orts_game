package de.vatterger.game.systems.graphics;

import com.artemis.BaseSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;

public class ShapeRenderSystem extends BaseSystem {

	private Camera camera;
	private ShapeRenderer shapeRenderer;
	
	//private SpriteBatch batch;
	//private BitmapFont font;
	
	private Vector3 v0 = new Vector3();
	private Vector3 v1 = new Vector3();
	
	public ShapeRenderSystem(Camera camera) {
		this.camera = camera;

		shapeRenderer = new ShapeRenderer(4096);
		//font = new BitmapFont();
		//batch = new SpriteBatch(64);
	}
	
	@Override
	protected void processSystem() {
		
		shapeRenderer.setProjectionMatrix(camera.combined/*.cpy().scl(1f, Metrics.ymodp, 1f)*/);
		shapeRenderer.setTransformMatrix(new Matrix4(new Vector3(0f, 0f, camera.position.y - 1024f),new Quaternion(Vector3.X, -45f), new Vector3(1f, 1f, 1f)));
		shapeRenderer.updateMatrices();
		shapeRenderer.begin(ShapeType.Line);

			shapeRenderer.setColor(Color.RED);
			v0.set(0f, 0f, 0f);
			v1.set(250f, 0f, 0f);
			shapeRenderer.line(v0, v1);
			
			shapeRenderer.setColor(Color.GREEN);
			v0.set(0f, 0f, 0f);
			v1.set(0f, 250f, 0f);
			shapeRenderer.line(v0, v1);
			
			shapeRenderer.setColor(Color.BLUE);
			v0.set(0f, 0f, 0f);
			v1.set(0f, 0f, 250f);
			shapeRenderer.line(v0, v1);
			
			shapeRenderer.setColor(Color.BLUE);
			v0.set(0f, 1000f, 0f);
			v1.set(0f, 1000f, 250f);
			shapeRenderer.line(v0, v1);
			
			shapeRenderer.setColor(Color.WHITE);
			shapeRenderer.box(120f, 130f, 0f, 10f, 10f, 10f);
			shapeRenderer.cone(125f, 125f, 10f, 5f, 10f, 16);
			
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 10; j++) {
					//shapeRenderer.setColor(Color.WHITE);
					//shapeRenderer.rect(250f * i, 250f * j, 250f, 250f);
				}
			}
			
			shapeRenderer.end();
	}
	
	@Override
	protected void dispose() {
		shapeRenderer.dispose();
	}
}
