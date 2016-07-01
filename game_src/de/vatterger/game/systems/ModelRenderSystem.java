package de.vatterger.game.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.unit.Model;
import de.vatterger.game.components.unit.Position;
import de.vatterger.game.components.unit.Rotation;

public class ModelRenderSystem extends IteratingSystem {

	ModelBatch	modelBatch;
	
	Camera		camera;
	Environment environment;

	private ComponentMapper<Position>	pm;
	private ComponentMapper<Rotation>	rm;
	private ComponentMapper<Model>		mm;
	
	public ModelRenderSystem(Camera camera, Environment environment) {
		super(Aspect.all(Model.class,Position.class, Rotation.class));
		this.camera = camera;
		this.environment = environment;
		modelBatch = new ModelBatch();
	}
	
	@Override
	protected void begin() {
		modelBatch.begin(camera);
	}

	protected void process(int e) {
		ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);
		
		Node node = instance.nodes.first();
		node.translation.set(pm.get(e).v);
		node.rotation.set(rm.get(e).v);
		
		instance.calculateTransforms();
		
		modelBatch.render(instance, environment);
	}

	@Override
	protected void end() {
		modelBatch.end();
	}
	
	@Override
	protected void dispose() {
		modelBatch.dispose();
	}
}
