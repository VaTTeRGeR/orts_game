package de.vatterger.game.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.util.GameUtil;
import de.vatterger.game.components.unit.Model;
import de.vatterger.game.components.unit.Position;
import de.vatterger.game.components.unit.Rotation;

public class ModelShadowProcessor extends IteratingSystem {

	private ComponentMapper<Position>	pm;
	private ComponentMapper<Rotation>	rm;
	private ComponentMapper<Model>	mm;
	
	private ModelBatch shadowBatch;
	private DirectionalShadowLight shadowLight;
	private Camera cam;
	
	@SuppressWarnings("unchecked")
	public ModelShadowProcessor(DirectionalShadowLight shadowLight, Camera cam) {
		super(Aspect.all(Position.class, Model.class, Rotation.class));
		this.shadowLight = shadowLight;
		this.cam = cam;
		shadowBatch = new ModelBatch(new DepthShaderProvider());
	}
	
	@Override
	protected void begin() {
		shadowLight.begin(GameUtil.intersectMouseGroundPlane(cam, Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f, 0).add(0f, 0f, 32f), shadowLight.direction);
		shadowBatch.begin(shadowLight.getCamera());
	}

	protected void process(int e) {
		ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);
		Node node = instance.nodes.first();
		node.translation.set(pm.get(e).v);
		node.rotation.set(rm.get(e).v);
		instance.calculateTransforms();
		shadowBatch.render(instance.getRenderable(new Renderable(), node));
	}
	
	@Override
	protected void end() {
		shadowBatch.end();
		shadowLight.end();
	}
}
