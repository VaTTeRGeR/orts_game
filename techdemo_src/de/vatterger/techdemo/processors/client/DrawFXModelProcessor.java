package de.vatterger.techdemo.processors.client;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.techdemo.application.GameConstants;
import de.vatterger.techdemo.components.client.AlphaBlend;
import de.vatterger.techdemo.components.client.LocalPosition;
import de.vatterger.techdemo.components.client.LocalVelocity;
import de.vatterger.techdemo.components.shared.G3DBModelId;
import de.vatterger.techdemo.components.shared.Inactive;
import de.vatterger.techdemo.components.shared.StaticModel;

@Wire
public class DrawFXModelProcessor extends EntityProcessingSystem {

	private ComponentMapper<LocalPosition>	lpm;
	private ComponentMapper<LocalVelocity>	lvm;
	private ComponentMapper<G3DBModelId>	gmim;
	private ComponentMapper<AlphaBlend>	abm;
	
	private ModelBatch batch;
	private Camera cam;
	private Environment environment;
		
	@SuppressWarnings("unchecked")
	public DrawFXModelProcessor(ModelBatch batch, Camera cam , Environment environment) {
		super(Aspect.all(LocalPosition.class, LocalVelocity.class, G3DBModelId.class, AlphaBlend.class).exclude(Inactive.class, StaticModel.class));
		this.batch = batch;
		this.cam = cam;
		this.environment = environment;
	}

	@Override
	protected void begin() {
		batch.begin(cam);
	}

	protected void process(Entity e) {
		if (cam.position.dst(lpm.get(e).pos) < GameConstants.NET_SYNC_THRESHOLD) {
			ModelInstance instance = ModelHandler.getSharedInstanceByID(gmim.get(e).id);
			Material mat = instance.materials.first();
			mat.set(abm.get(e).blendAttr);
			instance.nodes.first().translation.set(lpm.get(e).pos);
			Vector3 vel = lvm.get(e).vel;
			instance.nodes.first().rotation.set(Vector3.Z,MathUtils.atan2(vel.y, vel.x)*MathUtils.radDeg);
			instance.calculateTransforms();
			batch.render(instance, environment);
		}
	}
	
	@Override
	protected void end() {
		batch.end();
	}
}
