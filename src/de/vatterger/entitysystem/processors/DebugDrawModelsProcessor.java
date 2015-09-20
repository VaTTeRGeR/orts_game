package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.G3DBModelId;
import de.vatterger.entitysystem.components.Position;

public class DebugDrawModelsProcessor extends EntityProcessingSystem {

	ComponentMapper<Position>	pm;
	ComponentMapper<G3DBModelId>	gmim;
	
	ModelBatch batch;
	Camera cam;
	Environment environment;

	Bag<Model> models = new Bag<Model>(128);
	private AssetManager assetManager;

	@SuppressWarnings("unchecked")
	public DebugDrawModelsProcessor(ModelBatch batch, Camera cam , Environment environment, AssetManager assetManager) {
		super(Aspect.getAspectForAll(Position.class));
		this.batch = batch;
		this.cam = cam;
		this.environment = environment;
		this.assetManager = assetManager;
	}

	@Override
	protected void initialize() {
		pm = world.getMapper(Position.class);
		gmim = world.getMapper(G3DBModelId.class);
		
		assetManager.load("panzeri.g3db", Model.class);
		assetManager.finishLoading();
		models.add(assetManager.get("panzeri.g3db", Model.class));
	}

	@Override
	protected void begin() {
		batch.begin(cam);
	}

	protected void process(Entity e) {
		Position pc = pm.get(e);
		//G3DBModelId gmic = gmim.get(e);

		ModelInstance instance = new ModelInstance(models.get(0));
		instance.transform.translate(pc.pos);
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
