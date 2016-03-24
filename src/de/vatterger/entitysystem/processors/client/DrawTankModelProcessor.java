package de.vatterger.entitysystem.processors.client;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.application.GameConstants;
import de.vatterger.entitysystem.components.client.InterpolatedPosition;
import de.vatterger.entitysystem.components.client.InterpolatedRotation;
import de.vatterger.entitysystem.components.client.InterpolatedTurretRotation;
import de.vatterger.entitysystem.components.shared.G3DBModelId;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.StaticModel;
import de.vatterger.entitysystem.handler.asset.ModelHandler;

@Wire
public class DrawTankModelProcessor extends EntityProcessingSystem {

	private ComponentMapper<InterpolatedPosition>	cpm;
	private ComponentMapper<InterpolatedRotation>	crm;
	private ComponentMapper<InterpolatedTurretRotation>	itrm;
	private ComponentMapper<G3DBModelId>	gmim;
	
	private ModelBatch batch;
	private Camera cam;
	private Environment env;
	
	
	@SuppressWarnings("unchecked")
	public DrawTankModelProcessor(ModelBatch batch, Camera cam, Environment env) {
		super(Aspect.all(InterpolatedPosition.class, InterpolatedRotation.class, InterpolatedTurretRotation.class, G3DBModelId.class).exclude(Inactive.class, StaticModel.class));
		this.batch = batch;
		this.cam = cam;
		this.env = env;
	}

	@Override
	protected void begin() {
		batch.begin(cam);
	}
	
	protected void process(Entity e) {
		if (cam.position.dst(cpm.get(e).getInterpolatedValue()) < GameConstants.NET_SYNC_AREA) {
			ModelInstance instance = ModelHandler.getSharedInstanceByID(gmim.get(e).id);
			
			Node node = instance.getNode("hull");
			node.translation.set(cpm.get(e).getInterpolatedValue());
			node.rotation.set(Vector3.Z, crm.get(e).getInterpolatedValue());
			
			node = instance.getNode("turret");
			node.rotation.set(Vector3.Z, itrm.get(e).getInterpolatedValue());

			instance.calculateTransforms();
						
			batch.render(instance, env);
		}
	}
	
	@Override
	protected void end() {
		batch.end();
	}
}
