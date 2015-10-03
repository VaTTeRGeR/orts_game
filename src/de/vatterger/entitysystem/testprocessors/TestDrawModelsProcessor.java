package de.vatterger.entitysystem.testprocessors;

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

	ComponentMapper<ClientPosition>	cpm;
	ComponentMapper<ClientRotation>	crm;
	ComponentMapper<G3DBModelId>	gmim;
	
	ModelBatch batch;
	Camera cam;
	Environment environment;

	Bag<Model> models = new Bag<Model>(128);
	Bag<ModelInstance> modelInstances = new Bag<ModelInstance>(128);
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
		
		assetManager.load("panzeri.g3dj", Model.class);
		assetManager.finishLoading();
		models.add(assetManager.get("panzeri.g3dj", Model.class));
		modelInstances.add(new ModelInstance(models.get(0)));
	}

	@Override
	protected void begin() {
		batch.begin(cam);
	}

	protected void process(Entity e) {
		ModelInstance instance = new ModelInstance(models.get(gmim.get(e).id));
		instance.getNode("hull", true).translation.set(cpm.get(e).getInterpolatedValue());
		instance.getNode("hull", true).rotation.set(new Vector3(0f, 0f, 1f), crm.get(e).getInterpolatedValue());
		instance.getNode("turret", true).rotation.set(new Vector3(0f, 0f, 1f), crm.get(e).getInterpolatedValue());
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
