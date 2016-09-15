package de.vatterger.engine.handler.asset;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader.ModelParameters;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

import de.vatterger.engine.handler.asset.AssetPathFinder.AssetPath;

public final class ModelHandler {
	
	private static final HashMap<String, Integer> ntim; //name to id mapping
	private static final ArrayList<String> itnm; //id to name mapping
	private static final ArrayList<String> itpm; //id to path mapping

	private static final ArrayList<Model> models;  //id to model mapping
	private static final ArrayList<ModelInstance> instances;  //id to model-instance mapping

	public static final int DEFAULT_ID;
	public static final String DEFAULT_NAME;
	public static final String DEFAULT_PATH;
	
	public static AssetManager assetManager;

	static {
		ntim = new HashMap<String, Integer>();
		itnm = new ArrayList<String>();
		itpm = new ArrayList<String>();

		models = new ArrayList<Model>();
		instances = new ArrayList<ModelInstance>();
		
		DEFAULT_ID = register(DEFAULT_NAME = "default", DEFAULT_PATH = "models/default.g3db");
	}
	
	private ModelHandler() {}

	public static final void loadModels() {
		initialize();
		
		for (AssetPath ap : AssetPathFinder.searchForAssets("g3db")) {
			register(ap.name, ap.absolutePath);
		}

		AssetPathFinder.searchForAssets("g3dj", "models");
		
		loadRegisteredModels(ModelHandler.getAllModelPaths());
	}

	public static final void loadRegisteredModels(String[] paths) {
		for (int i = 0; i < paths.length; i++) {
			ModelParameters params = new ModelParameters();
			params.textureParameter.genMipMaps = true;
			params.textureParameter.magFilter = TextureFilter.Linear;
			params.textureParameter.minFilter = TextureFilter.MipMapLinearLinear;
			assetManager.load(paths[i], Model.class, params);
		}
		
		assetManager.finishLoading();
		
		for (int i = 0; i < paths.length; i++) {
			models.add(i, assetManager.get(paths[i], Model.class));
			instances.add(i,new ModelInstance(models.get(i)));
		}
	}
	
	public static final ModelInstance getSharedInstanceByID(int id) {
		return instances.get(id);
	}

	public static final ModelInstance getSharedInstanceByName(String name) {
		return getSharedInstanceByID(getModelId(name));
	}

	public static final ModelInstance getNewInstanceByID(int id) {
		return new ModelInstance(models.get(id));
	}

	public static final ModelInstance getNewInstanceByName(String name) {
		return getNewInstanceByID(getModelId(name));
	}

	public static final Model getModelByID(int id) {
		return models.get(id);
	}

	public static final Model getModelByName(String name) {
		return getModelByID(getModelId(name));
	}

	public static final Integer getModelId(String name) {
		Integer i = ntim.get(name);
		if(i == null)
			return DEFAULT_ID;
		else
			return i;
	}

	public static final String getModelName(int id) {
		String s = itnm.get(id);
		if(s == null)
			return DEFAULT_NAME;
		else
			return s;
	}
	
	public static final String getModelPath(int id) {
		String s = itpm.get(id);
		if(s == null)
			return DEFAULT_PATH;
		else
			return s;
	}
	
	public static final String getModelPath(String name) {
		return getModelPath(getModelId(name));
	}
	
	public static final String[] getAllModelPaths() {
		return itpm.toArray(new String[itpm.size()]);
	}
	
	private static final int register(String name, String path){
		if(!ntim.containsKey(name)) {
			int n = ntim.size();

			ntim.put(name, n);
			
			itnm.add(n, name);
			itpm.add(n, path);
			
			return n;
		} else {
			int n = getModelId(name);

			itpm.set(n, path);

			return n;
		}
	}
	
	public static void initialize() {
		dispose();
		assetManager = new AssetManager();
	}
	
	public static void dispose() {
		if(assetManager != null)
			assetManager.dispose();
	}
}
