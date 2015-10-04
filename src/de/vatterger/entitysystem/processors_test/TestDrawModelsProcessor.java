package de.vatterger.entitysystem.processors_test;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.G3DBModelId;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.ClientPosition;
import de.vatterger.entitysystem.components.ClientRotation;

public class TestDrawModelsProcessor extends EntityProcessingSystem {

	private ComponentMapper<ClientPosition>	cpm;
	private ComponentMapper<ClientRotation>	crm;
	private ComponentMapper<G3DBModelId>	gmim;
	
	private ModelBatch batch;
	private Camera cam;
	private Environment environment;

	private Bag<String> modelPaths = new Bag<String>();
	private Bag<Model> models = new Bag<Model>();
	private Bag<ModelInstance> modelInstances = new Bag<ModelInstance>();
	
	private AssetManager assetManager;

	@SuppressWarnings("unchecked")
	public TestDrawModelsProcessor(ModelBatch batch, Camera cam , Environment environment, AssetManager assetManager) {
		super(Aspect.getAspectForAll(ClientPosition.class, G3DBModelId.class, ClientRotation.class).exclude(Inactive.class));
		this.batch = batch;
		this.cam = cam;
		this.environment = environment;
		this.assetManager = assetManager;
	}

	@Override
	protected void initialize() {
		cpm = world.getMapper(ClientPosition.class);
		crm = world.getMapper(ClientRotation.class);
		gmim = world.getMapper(G3DBModelId.class);
		
		modelPaths.add("panzeri.g3dj");
		
		for (int i = 0; i < modelPaths.size(); i++) {
			assetManager.load(modelPaths.get(i), Model.class);
		}
		
		assetManager.finishLoading();
		
		for (int i = 0; i < modelPaths.size(); i++) {
			models.add(assetManager.get(modelPaths.get(i), Model.class));
		}
		
		for (int i = 0; i < models.size(); i++) {
			modelInstances.add(new ModelInstance(models.get(i)));
		}
	}

	@Override
	protected void begin() {
		batch.begin(cam);
	}

	protected void process(Entity e) {
		//ModelInstance instance = new ModelInstance(models.get(gmim.get(e).id));
		ModelInstance instance = modelInstances.get(gmim.get(e).id);
		instance.getNode("hull").translation.set(cpm.get(e).getInterpolatedValue());
		instance.getNode("hull").rotation.set(new Vector3(0f, 0f, 1f), crm.get(e).getInterpolatedValue());
		instance.calculateTransforms();
		batch.render(instance, environment);
	}
	
	@Override
	protected void end() {
		batch.end();
	}

	@Override
	protected void dispose() {
	}
}
