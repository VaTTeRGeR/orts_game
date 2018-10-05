package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.TerrainHeightField;

public class ShapeRenderSystem extends IteratingSystem {

	private Camera camera;
	private ShapeRenderer shapeRenderer;
	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<CullDistance> cdm;
	
	//private SpriteBatch batch;
	//private BitmapFont font;
	
	//private Vector3 v0 = new Vector3();
	//private Vector3 v1 = new Vector3();
	
	public ShapeRenderSystem(Camera camera) {

		super(Aspect.all(AbsolutePosition.class, CullDistance.class).exclude(Culled.class, Attached.class));
		
		this.camera = camera;

		shapeRenderer = new ShapeRenderer(4096);
		//font = new BitmapFont();
		//batch = new SpriteBatch(64);
	}
	
	@Override
	protected void begin() {
		
		shapeRenderer.setProjectionMatrix(camera.combined/*.cpy().scl(1f, Metrics.ymodp, 1f)*/);
		shapeRenderer.setTransformMatrix(new Matrix4(new Vector3(0f, 0f, camera.position.y - 1024f),new Quaternion(Vector3.X, -45f), new Vector3(1f, 1f, 1f)));
		shapeRenderer.updateMatrices();
		shapeRenderer.begin(ShapeType.Line);

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
		
		//Vector3 mousePos = Math2D.castMouseRay(new Vector3(), camera);
		//shapeRenderer.setColor(Color.WHITE);
		//shapeRenderer.circle(mousePos.x, mousePos.y, 8);
		
	}
	
	@Override
	protected void process(int e) {
		
		Vector3 ap = apm.get(e).position;
		CullDistance cd = cdm.get(e);
		float cdr = cd.dst;
		float ox = cd.offsetX;
		float oy = cd.offsetY;
		
		shapeRenderer.setColor(Color.WHITE);
		shapeRenderer.rect(ap.x - cdr + ox, ap.y - cdr + oy, 2f * cdr, 2f * cdr);
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
