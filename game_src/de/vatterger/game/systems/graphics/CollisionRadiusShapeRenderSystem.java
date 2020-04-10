package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.CollisionRadius;
import de.vatterger.game.components.gameobject.Culled;

public class CollisionRadiusShapeRenderSystem extends IteratingSystem {

	@Wire(name="camera")
	private Camera camera;
	private ShapeRenderer shapeRenderer;
	
	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<CollisionRadius> crm;
	
	//private SpriteBatch batch;
	//private BitmapFont font;
	
	//private Vector3 v0 = new Vector3();
	//private Vector3 v1 = new Vector3();
	
	public CollisionRadiusShapeRenderSystem() {

		super(Aspect.all(AbsolutePosition.class, CollisionRadius.class).exclude(Culled.class, Attached.class));
		
		shapeRenderer = new ShapeRenderer(8192);
		//font = new BitmapFont();
		//batch = new SpriteBatch(64);
	}
	
	@Override
	protected void begin() {
		
		shapeRenderer.setProjectionMatrix(camera.combined/*.cpy().scl(1f, Metrics.ymodp, 1f)*/);
		shapeRenderer.setTransformMatrix(new Matrix4(new Vector3(0f, 0f, camera.position.y - 1024f),new Quaternion(Vector3.X, -45f), new Vector3(1f, 1f, 1f)));
		shapeRenderer.updateMatrices();
		shapeRenderer.begin(ShapeType.Line);

		shapeRenderer.setColor(Color.RED);
		
		/*shapeRenderer.setColor(Color.GREEN);
		v0.set(0f, 0f, 0f);
		v1.set(0f, 10f, 0f);
		shapeRenderer.line(v0, v1);

		shapeRenderer.setColor(Color.RED);
		v0.set(0f, 10f, 0f);
		v1.set(10f, 10f, 0f);
		shapeRenderer.line(v0, v1);

		shapeRenderer.setColor(Color.BLUE);
		v0.set(10f, 10f, 0f);
		v1.set(10f, 10f, 10f);
		shapeRenderer.line(v0, v1);*/
			
	}
	
	@Override
	protected void process(int e) {
		
		Vector3 ap = apm.get(e).position;
		CollisionRadius cr = crm.get(e);
		float crr = cr.dst;
		
		shapeRenderer.circle(ap.x, ap.y, crr, 24);
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
