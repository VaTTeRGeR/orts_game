package de.vatterger.entitysystem.processors.experimental;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.G3DBModelId;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.StaticModel;
import de.vatterger.entitysystem.components.client.ClientPosition;
import de.vatterger.entitysystem.components.client.ClientRotation;
import de.vatterger.entitysystem.handler.asset.ModelRegister;

@Wire
public class TestDrawStaticModelsProcessor extends EntityProcessingSystem {

	private ComponentMapper<ClientPosition>	cpm;
	private ComponentMapper<ClientRotation>	crm;
	private ComponentMapper<G3DBModelId>	gmim;
	
	private ModelBatch batch;
	private ModelCache cache;
	private Camera cam;
	private Environment environment;

	private boolean needStaticModelRebuild = false;
	
	@SuppressWarnings("unchecked")
	public TestDrawStaticModelsProcessor(ModelBatch batch, Camera cam , Environment environment) {
		super(Aspect.getAspectForAll(ClientPosition.class, G3DBModelId.class, ClientRotation.class, StaticModel.class).exclude(Inactive.class));
		
		this.batch = batch;
		this.cam = cam;
		this.environment = environment;

		cache = new ModelCache();
	}

	@Override
	protected void inserted(Entity e) {
		needStaticModelRebuild = true;
	}
	
	@Override
	protected void removed(Entity e) {
		needStaticModelRebuild = true;
	}
	
	@Override
	protected void begin() {
		if(needStaticModelRebuild)
			cache.begin(cam);
	}

	protected void process(Entity e) {
		if (needStaticModelRebuild) {
			ModelInstance instance = ModelRegister.getByID(gmim.get(e).id);
			instance.nodes.first().translation.set(cpm.get(e).getInterpolatedValue());
			instance.nodes.first().rotation.set(new Vector3(0f, 0f, 1f), crm.get(e).getInterpolatedValue());
			instance.calculateTransforms();
			cache.add(instance);
		}
	}
	
	@Override
	protected void end() {
		if(needStaticModelRebuild){
			cache.end();
		}
		batch.begin(cam);
		batch.render(cache, environment);
		batch.end();
		needStaticModelRebuild = false;
	}
}
