package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Model;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;

public class ModelDebugRenderSystem extends IteratingSystem {

	private Camera camera;
	private ImmediateModeRenderer20 immediateRenderer;


	private ComponentMapper<Position>	pm;
	private ComponentMapper<Rotation>	rm;
	private ComponentMapper<Model>		mm;
	private ComponentMapper<CullDistance>		cdm;
	
	Vector3 v1 = new Vector3();
	Vector3 v2 = new Vector3();
	Queue<Node> q = new Queue<Node>(16);
	
	boolean toggleRender = false;
	
	public ModelDebugRenderSystem(ImmediateModeRenderer20 immediateRenderer, Camera camera) {
		super(Aspect.all(Model.class, Position.class, Rotation.class));
		this.camera = camera;
		this.immediateRenderer = immediateRenderer;
		this.camera = camera;
	}
	
	@Override
	protected void begin() {
		super.begin();
		if(Gdx.input.isKeyJustPressed(Keys.F1))
			toggleRender = !toggleRender;
	}
	
	protected void process(int e) {
		if (toggleRender && (!cdm.has(e) || camera.frustum.sphereInFrustum(v1.set(pm.get(e).v), cdm.get(e).v))) {

			ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);

			Array<Node> nodes = instance.nodes;

			nodes.first().translation.set(v1.set(pm.get(e).v));
			nodes.first().rotation.set(rm.get(e).v[0]);

			instance.calculateTransforms();

			v2.set(2f, 0f, 0f).rotate(Vector3.Z, rm.get(e).v[0].getAngleAround(Vector3.Z)).add(v1);

			immediateRenderer.begin(camera.combined, GL20.GL_LINES);
			immediateRenderer.color(Color.WHITE);
			immediateRenderer.vertex(v1.x, v1.y, v1.z);
			immediateRenderer.color(Color.WHITE);
			immediateRenderer.vertex(v2.x, v2.y, v2.z);
			immediateRenderer.end();

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

					immediateRenderer.begin(camera.combined, GL20.GL_LINES);
					immediateRenderer.color(Color.RED);
					immediateRenderer.vertex(v1.x, v1.y, v1.z);
					immediateRenderer.color(Color.YELLOW);
					immediateRenderer.vertex(v2.x, v2.y, v2.z);
					immediateRenderer.end();
				}

				immediateRenderer.begin(camera.combined, GL20.GL_LINES);
				immediateRenderer.color(Color.BLUE);
				immediateRenderer.vertex(v1.x, v1.y, v1.z - 0.1f);
				immediateRenderer.color(Color.BLUE);
				immediateRenderer.vertex(v1.x, v1.y, v1.z + 0.1f);
				immediateRenderer.end();

				immediateRenderer.begin(camera.combined, GL20.GL_LINES);
				immediateRenderer.color(Color.GREEN);
				immediateRenderer.vertex(v1.x, v1.y - 0.1f, v1.z);
				immediateRenderer.color(Color.GREEN);
				immediateRenderer.vertex(v1.x, v1.y + 0.1f, v1.z);
				immediateRenderer.end();

				immediateRenderer.begin(camera.combined, GL20.GL_LINES);
				immediateRenderer.color(Color.RED);
				immediateRenderer.vertex(v1.x - 0.1f, v1.y, v1.z);
				immediateRenderer.color(Color.RED);
				immediateRenderer.vertex(v1.x + 0.1f, v1.y, v1.z);
				immediateRenderer.end();
			}
		}
	}
}
