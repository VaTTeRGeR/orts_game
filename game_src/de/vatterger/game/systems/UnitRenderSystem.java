package de.vatterger.game.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.game.components.unit.Model;
import de.vatterger.game.components.unit.Position;
import de.vatterger.game.components.unit.Rotation;

public class UnitRenderSystem extends IteratingSystem {

	ModelBatch	batch;
	
	Camera		cam;
	Environment environment;

	private ComponentMapper<Position>	pm;
	private ComponentMapper<Rotation>	rm;
	private ComponentMapper<Model>		mm;
	
	public UnitRenderSystem(Camera cam, Environment environment) {
		super(Aspect.all(Model.class,Position.class, Rotation.class));
		this.cam = cam;
		this.environment = environment;
		batch = new ModelBatch();
	}
	
	@Override
	protected void begin() {
		batch.begin(cam);
	}

	protected void process(int e) {
		ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);
		
		Node node = instance.nodes.first();
		node.translation.set(pm.get(e).v);
		node.rotation.set(Vector3.Z, rm.get(e).v);
		instance.calculateTransforms();
		batch.render(instance, environment);
	}

	@Override
	protected void end() {
		batch.end();
	}
	
	@Override
	protected void dispose() {
		batch.dispose();
	}
}
