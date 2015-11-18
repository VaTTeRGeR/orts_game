package de.vatterger.entitysystem.registers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.artemis.utils.Bag;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader.ModelParameters;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public final class ModelStore {
	
	private static final ArrayList<Model> models;
	private static final ArrayList<ModelInstance> instances;

	static {
		models = new ArrayList<Model>();
		instances = new ArrayList<ModelInstance>();
	}
	
	private ModelStore() {}

	public static final void loadModels(AssetManager manager){
		String[] paths = ModelRegister.getAllModelPaths();
		
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
}
