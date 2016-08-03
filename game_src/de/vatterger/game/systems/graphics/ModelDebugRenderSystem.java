package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.util.NodeRotationUtil;
import de.vatterger.game.components.gameobject.ModelID;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;

public class ModelDebugRenderSystem extends IteratingSystem {

	private Camera camera;
	private ShapeRenderer shapeRenderer;


	private ComponentMapper<Position>	pm;
	private ComponentMapper<Rotation>	rm;
	private ComponentMapper<ModelID>		mm;
	
	Vector3 v1 = new Vector3();
	Vector3 v2 = new Vector3();
	Vector3 v3 = new Vector3();
	Queue<Node> q = new Queue<Node>(16);
	
	boolean toggleRender = false;
	
	public ModelDebugRenderSystem(Camera camera) {
		super(Aspect.all(ModelID.class, Position.class, Rotation.class));
		this.camera = camera;
		shapeRenderer = new ShapeRenderer(4096);
	}
	
	@Override
	protected void begin() {
		if(Gdx.input.isKeyJustPressed(Keys.F1))
			toggleRender = !toggleRender;
		if(toggleRender) {
			shapeRenderer.setProjectionMatrix(camera.combined);
			shapeRenderer.begin(ShapeType.Line);
		}
	}
	
	protected void process(int e) {
		if (toggleRender && camera.frustum.pointInFrustum(v1.set(pm.get(e).v))) {

			ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);

			Array<Node> nodes = instance.nodes;

			nodes.first().translation.set(v1.set(pm.get(e).v));
			NodeRotationUtil.setRotationByName(instance, rm.get(e));

			instance.calculateTransforms();

			v2.set(2f, 0f, 0f).rotate(Vector3.Z, rm.get(e).v1[0].getAngleAround(Vector3.Z)).add(v1);

			shapeRenderer.setColor(Color.WHITE);
			shapeRenderer.line(v1, v2);

			for (int i = 0; i < nodes.size; i++) {
				q.addLast(nodes.get(i));
			}

			while (q.size > 0) {
				Node x = q.removeFirst();
				x.globalTransform.getTranslation(v1);

				for (int i = 0; i < x.getChildCount(); i++) {
					Node y = x.getChild(i);
					y.globalTransform.getTranslation(v2);

					q.addLast(y);

					shapeRenderer.setColor(Color.YELLOW);
					shapeRenderer.line(v1, v2);
				}

				shapeRenderer.setColor(Color.RED);
				shapeRenderer.line(v2.set(v1).add(-0.1f, 0f, 0f), v3.set(v1).add(0.1f, 0f, 0f));

				shapeRenderer.setColor(Color.GREEN);
				shapeRenderer.line(v2.set(v1).add(0f, -0.1f, -0f), v3.set(v1).add(0f, 0.1f, 0f));
				
				shapeRenderer.setColor(Color.BLUE);
				shapeRenderer.line(v2.set(v1).add(0f, 0f, -0.1f), v3.set(v1).add(0f, 0f, 0.1f));
			}
		}
	}
	@Override
	protected void end() {
		if(toggleRender) {
			shapeRenderer.end();
		}
	}
}
