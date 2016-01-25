package de.vatterger.entitysystem.processors.client;

import java.util.concurrent.TimeUnit;

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

import de.vatterger.entitysystem.EntityFactory;
import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.components.client.InterpolatedPosition;
import de.vatterger.entitysystem.components.client.InterpolatedRotation;
import de.vatterger.entitysystem.components.client.InterpolatedTurretRotation;
import de.vatterger.entitysystem.components.shared.G3DBModelId;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.StaticModel;
import de.vatterger.entitysystem.handler.asset.ModelHandler;
import de.vatterger.entitysystem.util.Profiler;
import de.vatterger.entitysystem.util.Timer;

@Wire
public class DrawTankModelProcessor extends EntityProcessingSystem {

	private ComponentMapper<InterpolatedPosition>	cpm;
	private ComponentMapper<InterpolatedRotation>	crm;
	private ComponentMapper<InterpolatedTurretRotation>	itrm;
	private ComponentMapper<G3DBModelId>	gmim;
	
	private Profiler	profT = new Profiler("Transform and build Batch", TimeUnit.MICROSECONDS),
						profR = new Profiler("Render Batch", TimeUnit.MICROSECONDS);
	
	private ModelBatch batch;
	private Camera cam;
	private Environment env;
	
	
	@SuppressWarnings("unchecked")
	public DrawTankModelProcessor(ModelBatch batch, Camera cam, Environment env) {
		super(Aspect.getAspectForAll(InterpolatedPosition.class, G3DBModelId.class, InterpolatedRotation.class, InterpolatedTurretRotation.class).exclude(Inactive.class, StaticModel.class));
		this.batch = batch;
		this.cam = cam;
		this.env = env;
	}

	@Override
	protected void begin() {
		profT.start();
		t.tick(world.getDelta());
		batch.begin(cam);
	}

	Timer t = new Timer(1f/(200f/60f));
	
	protected void process(Entity e) {
		if (cam.position.dst(cpm.get(e).getInterpolatedValue()) < GameConstants.NET_SYNC_AREA) {
			ModelInstance instance = ModelHandler.getByID(gmim.get(e).id);
			
			Node node = instance.getNode("hull");
			node.translation.set(cpm.get(e).getInterpolatedValue());
			node.rotation.set(new Vector3(0f, 0f, 1f), crm.get(e).getInterpolatedValue());
			
			node = instance.getNode("turret");
			node.rotation.set(new Vector3(0f, 0f, 1f), itrm.get(e).getInterpolatedValue());

			instance.calculateTransforms();
			
			
			if(t.isActive()) {
				EntityFactory.createTracer(world, instance.getNode("gun1"), 250, "tracer_panzeri");
				EntityFactory.createTracer(world, instance.getNode("gun2"), 250, "tracer_panzeri");
			}
			
			batch.render(instance, env);
		}
	}
	
	@Override
	protected void end() {
		if(t.isActive())
			t.reset();
		profT.log();
		profR.start();
		batch.end();
		profR.log();
	}
}
