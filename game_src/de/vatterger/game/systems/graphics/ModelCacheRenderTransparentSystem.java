package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.ShadowMap;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.Model;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;
import de.vatterger.game.components.gameobject.StaticModel;
import de.vatterger.game.components.gameobject.Transparent;

@SuppressWarnings("deprecation")
public class ModelCacheRenderTransparentSystem extends IteratingSystem {

	private ComponentMapper<Model> mm;
	private ComponentMapper<Position> pm;
	private ComponentMapper<Rotation> rm;
	private ComponentMapper<Transparent> tm;
	
	private ModelBatch modelBatch;
	private ModelCache modelCache;
	private Camera cam;
	private Environment env;

	private FloatAttribute alphaTest = FloatAttribute.createAlphaTest(0.5f);
	private BlendingAttribute blendAttribute = new BlendingAttribute();

	private boolean needStaticModelRebuild = false;
	
	public ModelCacheRenderTransparentSystem(Camera camera , Environment environment) {
		super(Aspect.all(Position.class, Model.class, Rotation.class, StaticModel.class, Transparent.class));
		
		this.cam = camera;
		this.env = environment;

		modelBatch = new ModelBatch();
		modelCache = new ModelCache(new ModelCache.Sorter(), new ModelCache.TightMeshPool());
	}
	
	@Override
	public void inserted(int e) {
		needStaticModelRebuild = true;
	}
	
	@Override
	public void removed(int e) {
		needStaticModelRebuild = true;
	}
	
	Profiler p = new Profiler("cache build");
	
	@Override
	protected void begin() {
		if(needStaticModelRebuild) {
			modelCache.begin(cam);
			p.start();
		}
	}

	protected void process(int e) {
		if (needStaticModelRebuild) {
			ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);
			
			if(tm.get(e).v) {
				instance.materials.first().set(alphaTest);
			} else if(instance.materials.first().has(FloatAttribute.AlphaTest)) {
				instance.materials.first().remove(FloatAttribute.AlphaTest);
			}
			instance.materials.first().set(blendAttribute);

			instance.nodes.first().translation.set(pm.get(e).v);
			instance.nodes.first().rotation.set(rm.get(e).v);

			instance.calculateTransforms();
			
			modelCache.add(instance);
		}
	}
	
	@Override
	protected void end() {
		if(needStaticModelRebuild){
			modelCache.end();
			p.log();
		}

		ShadowMap shadowMap = env.shadowMap;
		env.shadowMap = null;

		modelBatch.begin(cam);
		modelBatch.render(modelCache, env);
		modelBatch.end();
		
		env.shadowMap = shadowMap;
		
		needStaticModelRebuild = false;
	}
}
