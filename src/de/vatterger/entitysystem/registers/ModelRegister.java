package de.vatterger.entitysystem.registers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public final class ModelRegister {
	
	private static final HashMap<String, Integer> ntim;
	private static final ArrayList<String> itnm;
	private static final ArrayList<String> itpm;

	public static final int DEFAULT_ID;
	public static final String DEFAULT_NAME;
	public static final String DEFAULT_PATH;

	static {
		ntim = new HashMap<String, Integer>();
		itnm = new ArrayList<String>();
		itpm = new ArrayList<String>();

		DEFAULT_ID = register(DEFAULT_NAME = "default", DEFAULT_PATH = "default.g3db");
		register("panzeri", "panzeri.g3db");
		register("terrain", "terrain.g3db");
	}
	
	private ModelRegister() {}

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
