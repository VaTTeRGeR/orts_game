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

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.model.ResponsiveModelCache;
import de.vatterger.engine.util.NodeRotationUtil;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.ModelID;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;
import de.vatterger.game.components.gameobject.StaticModel;
import de.vatterger.game.components.gameobject.Transparent;

public class ModelCacheRenderTransparentSystem extends IteratingSystem {

	private ComponentMapper<ModelID> mm;
	private ComponentMapper<Position> pm;
	private ComponentMapper<Rotation> rm;
	private ComponentMapper<Transparent> tm;
	
	private ModelBatch modelBatch;
	private ResponsiveModelCache modelCache;
	private Camera cam;
	private Environment env;

	private FloatAttribute alphaTest = FloatAttribute.createAlphaTest(0.5f);
	private BlendingAttribute blendAttribute = new BlendingAttribute();

	private boolean needStaticModelRebuild = false;
	private boolean isBuilding = false;
	private boolean isIdle = true;
	
	public ModelCacheRenderTransparentSystem(Camera camera , Environment environment) {
		super(Aspect.all(Position.class, ModelID.class, Rotation.class, StaticModel.class, Transparent.class, CullDistance.class));
		
		this.cam = camera;
		this.env = new Environment();
		this.env.set(environment);

		modelBatch = new ModelBatch();
		modelCache = new ResponsiveModelCache(new ResponsiveModelCache.Sorter(), new ResponsiveModelCache.TightMeshPool());
	}
	
	@Override
	public void inserted(int e) {
		isIdle = false;
		if(!isBuilding) needStaticModelRebuild = true;
	}
	
	@Override
	public void removed(int e) {
		isIdle = false;
		if(!isBuilding) needStaticModelRebuild = true;
	}
	
	@Override
	protected void begin() {
		if(needStaticModelRebuild && !isBuilding && isIdle) {
			modelCache.begin(cam);
		}
	}

	protected void process(int e) {
		if (needStaticModelRebuild && !isBuilding && isIdle) {
			ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);
			
			if(tm.get(e).v) {
				instance.materials.first().set(alphaTest);
			} else if(instance.materials.first().has(FloatAttribute.AlphaTest)) {
				instance.materials.first().remove(FloatAttribute.AlphaTest);
			}
			instance.materials.first().set(blendAttribute);

			instance.nodes.first().translation.set(pm.get(e).v);
			NodeRotationUtil.setRotationByName(instance, rm.get(e));

			instance.calculateTransforms();
			
			modelCache.add(instance);
		}
	}
	
	@Override
	protected void end() {
		if(needStaticModelRebuild && !isBuilding && isIdle){
			modelCache.build_begin();
			needStaticModelRebuild = false;
			isBuilding = true;
		}

		if(isBuilding) {
			if(modelCache.build_update(0.001f)) {
				isBuilding = false;
			}
		} else if (isIdle) {
			modelBatch.begin(cam);
			modelBatch.render(modelCache, env);
			modelBatch.end();
		}
		isIdle = true;
	}
}
