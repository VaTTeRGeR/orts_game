package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.G3DBModelId;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.Rotation;

public class DebugDrawModelsProcessor extends EntityProcessingSystem {

	ComponentMapper<Position>	pm;
	ComponentMapper<Rotation>	rm;
	ComponentMapper<G3DBModelId>	gmim;
	float turretDir = 0f;
	
	ModelBatch batch;
	Camera cam;
	Environment environment;

	Bag<Model> models = new Bag<Model>(128);
	Bag<ModelInstance> modelInstances = new Bag<ModelInstance>(128);
	private AssetManager assetManager;

	@SuppressWarnings("unchecked")
	public DebugDrawModelsProcessor(ModelBatch batch, Camera cam , Environment environment, AssetManager assetManager) {
		super(Aspect.getAspectForAll(Position.class, G3DBModelId.class, Rotation.class).exclude(Inactive.class));
		this.batch = batch;
		this.cam = cam;
		this.environment = environment;
		this.assetManager = assetManager;
	}

	@Override
	protected void initialize() {
		pm = world.getMapper(Position.class);
		rm = world.getMapper(Rotation.class);
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
		instance.getNode("hull", true).rotation.set(new Vector3(0f, 0f, 1f), rm.get(e).rot);
		instance.getNode("hull", true).translation.set(pm.get(e).pos);
		instance.getNode("turret", true).rotation.set(new Vector3(0f, 0f, 1f), turretDir);
		instance.calculateTransforms();
		batch.render(instance, environment);
	}
	
	@Override
	protected void end() {
		turretDir+=180*world.getDelta();
		if(turretDir > 360) {
			turretDir -= 360;
		}
		batch.end();
	}

	@Override
	protected void dispose() {
	}
}
