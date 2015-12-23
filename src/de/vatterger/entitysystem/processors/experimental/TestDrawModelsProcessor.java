package de.vatterger.entitysystem.processors.experimental;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.components.client.ClientPosition;
import de.vatterger.entitysystem.components.client.ClientRotation;
import de.vatterger.entitysystem.components.shared.G3DBModelId;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.StaticModel;
import de.vatterger.entitysystem.handler.asset.ModelHandler;

@Wire
public class TestDrawModelsProcessor extends EntityProcessingSystem {

	private ComponentMapper<ClientPosition>	cpm;
	private ComponentMapper<ClientRotation>	crm;
	private ComponentMapper<G3DBModelId>	gmim;
	
	private ModelBatch batch;
	private Camera cam;
	private Environment environment;
	
	@SuppressWarnings("unchecked")
	public TestDrawModelsProcessor(ModelBatch batch, Camera cam , Environment environment) {
		super(Aspect.getAspectForAll(ClientPosition.class, G3DBModelId.class, ClientRotation.class).exclude(Inactive.class, StaticModel.class));
		this.batch = batch;
		this.cam = cam;
		this.environment = environment;
	}

	@Override
	protected void begin() {
		batch.begin(cam);
	}

	protected void process(Entity e) {
		if (cam.position.dst(cpm.get(e).getInterpolatedValue()) < GameConstants.NET_SYNC_AREA) {
			ModelInstance instance = ModelHandler.getByID(gmim.get(e).id);
			instance.nodes.first().translation.set(cpm.get(e).getInterpolatedValue());
			instance.nodes.first().rotation.set(new Vector3(0f, 0f, 1f), crm.get(e).getInterpolatedValue());
			instance.calculateTransforms();
			batch.render(instance, environment);
		}
	}
	
	@Override
	protected void end() {
		batch.end();
	}
}
