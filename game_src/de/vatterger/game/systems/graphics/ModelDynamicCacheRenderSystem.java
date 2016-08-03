package de.vatterger.game.systems.graphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.util.NodeRotationUtil;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.ModelID;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;
import de.vatterger.game.components.gameobject.StaticModel;
import de.vatterger.game.components.gameobject.Transparent;

public class ModelDynamicCacheRenderSystem extends IteratingSystem {

	private ComponentMapper<ModelID> mm;
	private ComponentMapper<Position> pm;
	private ComponentMapper<Rotation> rm;
	private ComponentMapper<CullDistance> cdm;
	
	
	private ArrayList<ModelCache> caches = new ArrayList<ModelCache>(32);
	private LinkedList<Integer> modelQueue = new LinkedList<Integer>();
	private HashMap<Integer, ModelCache> modelToCacheMap = new HashMap<Integer, ModelCache>(256);
	private HashMap<ModelCache, BoundingBox> cacheToBoundsMap = new HashMap<ModelCache, BoundingBox>(64);
	private HashMap<ModelCache, Integer[]> cacheToModelMap = new HashMap<ModelCache, Integer[]>(64);
	private ModelBatch modelBatch;
	private Camera cam;
	private Environment environment;

	private static final int CACHE_BUILD_THRESHOLD = 64;
	private static final int VERTEX_BUILD_THRESHOLD = 1024*4;

	@SuppressWarnings("unchecked")
	public ModelDynamicCacheRenderSystem(Camera camera , Environment environment) {
		super(Aspect.all(Position.class, ModelID.class, Rotation.class, StaticModel.class, CullDistance.class).exclude(Transparent.class));
		
		this.cam = camera;
		this.environment = environment;

		modelBatch = new ModelBatch();
	}
	
	@Override
	public void inserted(int e) {
		if(world.getEntity(e).isActive())
			modelQueue.addLast(e);
	}
	
	@Override
	public void removed(int e) {
		ModelCache cache = modelToCacheMap.get(e);
		if(cache != null) {
			Integer[] ids = cacheToModelMap.get(cache);
			for (int i = 0; i < ids.length; i++) {
				modelToCacheMap.remove(ids[i]);
				if(ids[i] != null)
					inserted(ids[i]);
			}
			cacheToModelMap.remove(cache);
			cacheToBoundsMap.remove(cache);
			caches.remove(cache);
			cache.dispose();
		}
	}
	
	Profiler p = new Profiler("cache build");
	
	@Override
	protected void begin() {
		
		int i = 256;
		int v = VERTEX_BUILD_THRESHOLD;

		if(!modelQueue.isEmpty() && modelQueue.size() >= CACHE_BUILD_THRESHOLD && v > 0) {
			p.start();

			final ModelCache cache = new ModelCache(new ModelCache.Sorter(), new ModelCache.TightMeshPool());
			final Integer[] cachedIds = new Integer[i];
			final BoundingBox bounds = new BoundingBox();
			final Vector3 pos = new Vector3();
			
			cache.begin(cam);
			
			while (!modelQueue.isEmpty() && i-- > 0 && v > 0) {
				int e = modelQueue.pollFirst();

				if(!world.getEntity(e).isActive())
					continue;
				
				final ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);

				v -= ModelHandler.getModelByID(mm.get(e).id).meshes.first().getNumVertices();

				pos.set(pm.get(e).v);
				
				instance.nodes.first().translation.set(pos);
				NodeRotationUtil.setRotationByName(instance, rm.get(e));
				instance.calculateTransforms();
				
				cache.add(instance);

				cachedIds[i] = e;
				modelToCacheMap.put(e, cache);

				bounds.ext(pos, cdm.get(e).v);
			}

			cache.end();

			cacheToModelMap.put(cache, cachedIds);
			cacheToBoundsMap.put(cache, bounds);
			caches.add(cache);
			p.log();
		}
	}

	protected void process(int e) {}
	
	@Override
	protected void end() {
		modelBatch.begin(cam);

		for (Integer e : modelQueue) {
			if(!world.getEntity(e).isActive())
				continue;

			ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);

			instance.nodes.first().translation.set(pm.get(e).v);
			NodeRotationUtil.setRotationByName(instance, rm.get(e));

			instance.calculateTransforms();
			
			modelBatch.render(instance, environment);
		}
		
		for (int i = 0; i < caches.size(); i++) {
			if(cam.frustum.boundsInFrustum(cacheToBoundsMap.get(caches.get(i)))) {
				modelBatch.render(caches.get(i), environment);
			}
		}
		
		modelBatch.end();
	}
}
