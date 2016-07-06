package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Model;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;
import de.vatterger.game.components.gameobject.StaticModel;
import de.vatterger.game.components.gameobject.Transparent;

public class ModelRenderSystem extends IteratingSystem {

	private ModelBatch	modelBatch;
	
	private Camera		camera;
	private Environment environment;

	private ComponentMapper<Position> pm;
	private ComponentMapper<Rotation> rm;
	private ComponentMapper<Model> mm;
	private ComponentMapper<CullDistance> cdm;
	
	private Vector3 flyWeightVector3 = new Vector3();

	@SuppressWarnings("unchecked")
	public ModelRenderSystem(Camera camera, Environment environment) {
		super(Aspect.all(Model.class,Position.class, Rotation.class, CullDistance.class).exclude(Transparent.class, StaticModel.class));
		this.camera = camera;
		this.environment = environment;
		modelBatch = new ModelBatch();
	}
	
	@Override
	protected void begin() {
		modelBatch.begin(camera);
	}

	protected void process(int e) {
		if(camera.frustum.sphereInFrustum(flyWeightVector3.set(pm.get(e).v), cdm.get(e).v)) {
			ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);

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
	}
	
	@Override
	protected void dispose() {
		modelBatch.dispose();
	}
}
