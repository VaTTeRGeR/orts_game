package de.vatterger.game.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.environment.ShadowMap;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Model;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;
import de.vatterger.game.components.gameobject.Transparent;

@SuppressWarnings("deprecation")
public class TransparentModelRenderSystem extends IteratingSystem {

	private ModelBatch	modelBatch;
	
	private Camera		camera;
	private Environment environment;

	private ComponentMapper<Position>	pm;
	private ComponentMapper<Rotation>	rm;
	private ComponentMapper<Model>		mm;
	private ComponentMapper<CullDistance>		cdm;
	
	private Vector3 flyWeightVector3 = new Vector3();
	
	private ShadowMap shadowMap = null;

	public TransparentModelRenderSystem(Camera camera, Environment environment) {
		super(Aspect.all(Model.class,Position.class, Rotation.class, CullDistance.class, Transparent.class));
		this.camera = camera;
		this.environment = environment;
		modelBatch = new ModelBatch();
	}
	
	@Override
	protected void begin() {
		shadowMap = environment.shadowMap;
		environment.shadowMap = null;
		modelBatch.begin(camera);
	}

	protected void process(int e) {
		if(camera.frustum.sphereInFrustum(flyWeightVector3.set(pm.get(e).v), cdm.get(e).v)) {
			ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);

			instance.materials.first().set(new BlendingAttribute(true,1f));
			
			Node node = instance.nodes.first();
			node.translation.set(flyWeightVector3);
			node.rotation.set(rm.get(e).v);

			instance.calculateTransforms();

			modelBatch.render(instance, environment);
		}
	}

	@Override
	protected void end() {
		modelBatch.end();
		environment.shadowMap = shadowMap;
	}
	
	@Override
	protected void dispose() {
		modelBatch.dispose();
	}
}
