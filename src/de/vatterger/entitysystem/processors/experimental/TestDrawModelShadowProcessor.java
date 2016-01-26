package de.vatterger.entitysystem.processors.experimental;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.components.client.InterpolatedPosition;
import de.vatterger.entitysystem.components.client.InterpolatedRotation;
import de.vatterger.entitysystem.components.server.ServerTurretRotation;
import de.vatterger.entitysystem.components.shared.G3DBModelId;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.StaticModel;
import de.vatterger.entitysystem.handler.asset.ModelHandler;

@Wire
@SuppressWarnings("deprecation")
public class TestDrawModelShadowProcessor extends EntityProcessingSystem {

	private ComponentMapper<InterpolatedPosition>	cpm;
	private ComponentMapper<InterpolatedRotation>	crm;
	private ComponentMapper<ServerTurretRotation>	strm;
	private ComponentMapper<G3DBModelId>	gmim;
	
	private ModelBatch shadowBatch;
	private DirectionalShadowLight shadowLight;
	private Camera cam;
	
	@SuppressWarnings("unchecked")
	public TestDrawModelShadowProcessor(ModelBatch shadowBatch, DirectionalShadowLight shadowLight, Camera cam) {
		super(Aspect.getAspectForAll(InterpolatedPosition.class, G3DBModelId.class, InterpolatedRotation.class).exclude(Inactive.class, StaticModel.class));
		this.shadowBatch = shadowBatch;
		this.shadowLight = shadowLight;
		this.cam = cam;
	}

	@Override
	protected void begin() {
		shadowLight.begin(cam.position, cam.direction);
		shadowBatch.begin(shadowLight.getCamera());
	}

	protected void process(Entity e) {
		if (cam.position.dst(cpm.get(e).getInterpolatedValue()) < GameConstants.NET_SYNC_AREA) {
			ModelInstance instance = ModelHandler.getByID(gmim.get(e).id);
			instance.nodes.first().translation.set(cpm.get(e).getInterpolatedValue());
			instance.nodes.first().rotation.set(new Vector3(0f, 0f, 1f), crm.get(e).getInterpolatedValue());
			if(strm.has(e))
				instance.getNode("turret", true).rotation.set(new Vector3(0f, 0f, 1f), strm.get(e).rot);
			instance.calculateTransforms();
			shadowBatch.render(instance);
		}
	}
	
	@Override
	protected void end() {
		shadowBatch.end();
		shadowLight.end();
		Color sky = new Color(186f/255f, 232f/255f, 236f/255f, 1f);
		Gdx.gl.glClearColor(sky.r, sky.g, sky.b, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
	}
}
