package de.vatterger.entitysystem.handler.asset;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader.ModelParameters;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public final class ModelHandler {
	
	private static final HashMap<String, Integer> ntim;
	private static final ArrayList<String> itnm;
	private static final ArrayList<String> itpm;

	private static final ArrayList<Model> models;
	private static final ArrayList<ModelInstance> instances;

	public static final int DEFAULT_ID;
	public static final String DEFAULT_NAME;
	public static final String DEFAULT_PATH;

	static {
		ntim = new HashMap<String, Integer>();
		itnm = new ArrayList<String>();
		itpm = new ArrayList<String>();

		models = new ArrayList<Model>();
		instances = new ArrayList<ModelInstance>();

		DEFAULT_ID = register(DEFAULT_NAME = "default", DEFAULT_PATH = "default.g3db");
		register("panzeri", "panzeri.g3db");
		register("terrain", "terrain.g3db");
	}
	
	private ModelHandler() {}

	public static final void loadModels(AssetManager manager){
		String[] paths = ModelHandler.getAllModelPaths();
		
		for (int i = 0; i < paths.length; i++) {
			ModelParameters params = new ModelParameters();
			params.textureParameter.genMipMaps = true;
			params.textureParameter.magFilter = TextureFilter.Linear;
			params.textureParameter.minFilter = TextureFilter.MipMapLinearLinear;
			manager.load(paths[i], Model.class, params);
		}
		
		manager.finishLoading();
		
		for (int i = 0; i < paths.length; i++) {
			models.add(i, manager.get(paths[i], Model.class));
			instances.add(i,new ModelInstance(models.get(i)));
		}
	}

	public static final ModelInstance getByID(int id) {
		return instances.get(id);
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
			final int n = ntim.size();

			ntim.put(name, n);
			
			itnm.add(n, name);
			itpm.add(n, path);
			return n;
		}
		return DEFAULT_ID;
	}
}
