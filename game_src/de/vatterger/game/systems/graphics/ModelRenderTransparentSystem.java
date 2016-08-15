package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.util.NodeRotationUtil;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.ModelID;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;
import de.vatterger.game.components.gameobject.StaticModel;
import de.vatterger.game.components.gameobject.Transparent;

public class ModelRenderTransparentSystem extends IteratingSystem {

	private ModelBatch	modelBatch;
	
	private Camera		camera;
	private Environment environment;

	private ComponentMapper<Position>	pm;
	private ComponentMapper<Rotation>	rm;
	private ComponentMapper<ModelID>		mm;
	private ComponentMapper<CullDistance>		cdm;
	private ComponentMapper<Transparent>		tm;
	
	private Vector3 flyWeightVector3 = new Vector3();
	private FloatAttribute alphaTest = FloatAttribute.createAlphaTest(0.5f);
	private BlendingAttribute blendAttribute = new BlendingAttribute();

	@SuppressWarnings("unchecked")
	public ModelRenderTransparentSystem(Camera camera, Environment environment) {
		super(Aspect.all(ModelID.class,Position.class, Rotation.class, Transparent.class, CullDistance.class).exclude(StaticModel.class));
		this.camera = camera;
		this.environment = new Environment();
		this.environment.set(environment);
		modelBatch = new ModelBatch();
	}
	
	@Override
	protected void begin() {
		modelBatch.begin(camera);
	}

	protected void process(int e) {
		flyWeightVector3.set(pm.get(e).v);
		if(cdm.get(e).visible) {

			ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);
			
			if(tm.get(e).v) {
				instance.materials.first().set(alphaTest);
			} else if(instance.materials.first().has(FloatAttribute.AlphaTest)) {
				instance.materials.first().remove(FloatAttribute.AlphaTest);
			}
			instance.materials.first().set(blendAttribute);
			
			Node node = instance.nodes.first();
			node.translation.set(flyWeightVector3);
			NodeRotationUtil.setRotationByName(instance, rm.get(e));

			instance.calculateTransforms();

			modelBatch.render(instance, environment);
		}
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
