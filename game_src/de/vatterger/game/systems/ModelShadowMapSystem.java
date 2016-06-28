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

@SuppressWarnings("deprecation")
public class ModelShadowMapSystem extends IteratingSystem {

	private ComponentMapper<Position>	pm;
	private ComponentMapper<Rotation>	rm;
	private ComponentMapper<Model>		mm;
	
	private ModelBatch shadowModelBatch;
	private DirectionalShadowLight shadowLight;
	private Camera camera;
	
	public ModelShadowMapSystem(DirectionalShadowLight shadowLight, Camera camera) {
		super(Aspect.all(Position.class, Model.class, Rotation.class));
		this.shadowLight = shadowLight;
		this.camera = camera;
		shadowModelBatch = new ModelBatch(new DepthShaderProvider());
	}
	
	@Override
	protected void begin() {
		shadowLight.begin(GameUtil.intersectMouseGroundPlane(camera, Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f, 0).add(0f, 0f, 32f), shadowLight.direction);
		shadowModelBatch.begin(shadowLight.getCamera());
	}

	protected void process(int e) {
		ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);
		Node node = instance.nodes.first();
		node.translation.set(pm.get(e).v);
		node.rotation.set(rm.get(e).v);
		
		instance.calculateTransforms();
		
		shadowModelBatch.render(instance.getRenderable(new Renderable(), node));
	}
	
	@Override
	protected void end() {
		shadowModelBatch.end();
		shadowLight.end();
	}
}
