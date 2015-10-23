package de.vatterger.entitysystem.modelregister;

import java.util.HashMap;

public class ModelRegister {
	
	private static HashMap<String, Integer> ntim;
	private static HashMap<Integer, String> itnm;
	private static HashMap<Integer, String> itpm;
	private static int n;

	public static final int DEFAULT_ID;
	public static final String DEFAULT_NAME;
	public static final String DEFAULT_PATH;

	static {
		ntim = new HashMap<String, Integer>();
		itnm = new HashMap<Integer, String>();
		itpm = new HashMap<Integer, String>();
		n = 0;

		DEFAULT_NAME = "default";
		DEFAULT_PATH = "default.g3db";
		register(DEFAULT_NAME, DEFAULT_PATH);
		DEFAULT_ID = getModelId(DEFAULT_NAME);
		
		register("panzeri", "panzeri.g3db");
	}
	
	private ModelRegister() {}

	public static Integer getModelId(String name) {
		Integer i = ntim.get(name);
		if(i == null)
			return DEFAULT_ID;
		else
			return i;
	}

	public static String getModelName(int id) {
		String s = itnm.get(id);
		if(s == null)
			return DEFAULT_NAME;
		else
			return s;
	}
	
	public static String getModelPath(int id) {
		String s = itpm.get(id);
		if(s == null)
			return DEFAULT_PATH;
		else
			return s;
	}
	
	public static String getModelPath(String name) {
		return getModelPath(getModelId(name));
	}
	
	private static void register(String name, String path){
		ntim.put(name, n);
		itnm.put(n, name);
		itpm.put(n, path);
		n++;
	}
}
