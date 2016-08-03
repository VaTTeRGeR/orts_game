package de.vatterger.engine.handler.asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader.ModelParameters;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public final class ModelHandler {
	
	private static final HashMap<String, Integer> ntim; //name to id mapping
	private static final ArrayList<String> itnm; //id to name mapping
	private static final ArrayList<String> itpm; //id to path mapping

	private static final ArrayList<Model> models;  //id to model mapping
	private static final ArrayList<ModelInstance> instances;  //id to model-instance mapping

	public static final int DEFAULT_ID;
	public static final String DEFAULT_NAME;
	public static final String DEFAULT_PATH;
	
	private static AssetManager assetManager;

	static {
		ntim = new HashMap<String, Integer>();
		itnm = new ArrayList<String>();
		itpm = new ArrayList<String>();

		models = new ArrayList<Model>();
		instances = new ArrayList<ModelInstance>();
		
		DEFAULT_ID = register(DEFAULT_NAME = "default", DEFAULT_PATH = "models/default.g3db");
	}
	
	private ModelHandler() {}

	public static final void searchAndLoadModels() {
		dispose();

		assetManager = new AssetManager();
		
		searchAndRegisterModels();
		
		loadRegisteredModels(ModelHandler.getAllModelPaths());
	}

	private static final void searchAndRegisterModels() {
		FileHandle fileHandle = Gdx.files.internal("assets/models");
		if(fileHandle.exists() && fileHandle.isDirectory()) {
			try {
				Files.walk(fileHandle.file().toPath()).filter(Files::isRegularFile).filter(isModel()).forEach(ModelHandler::registerModelbyAbsolutePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static final Predicate<Path> isModel() {
	    return p -> p.toString().endsWith(".g3db");
	}
	
	private static final void registerModelbyAbsolutePath(Path p) {
		String absPath = p.toAbsolutePath().toString();
		
		//Positions of a, b and c
		// "[path to asset folder]a[path within assets]b+1[name]c[extension]"

		int a = absPath.lastIndexOf("assets");
		int b = absPath.lastIndexOf("\\") + 1;
		int c = absPath.lastIndexOf(".");
		
		String name = absPath.substring(b, c);
		String relPath = absPath.substring(a).replace("\\", "/");
		
		System.out.println(name + " - " + absPath + " - " + relPath);
		
		register(name, relPath);
	}

	public static final void loadRegisteredModels(String[] paths) {
		for (int i = 0; i < paths.length; i++) {
			ModelParameters params = new ModelParameters();
			params.textureParameter.genMipMaps = true;
			params.textureParameter.magFilter = TextureFilter.Linear;
			params.textureParameter.minFilter = TextureFilter.MipMapLinearNearest;
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
	
	public static void dispose() {
		if(assetManager != null)
			assetManager.dispose();
	}
}
