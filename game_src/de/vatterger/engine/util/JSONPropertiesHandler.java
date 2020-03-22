package de.vatterger.engine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.PrettyPrintSettings;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class JSONPropertiesHandler {
	
	private static final ConcurrentHashMap<String, JSONPropertiesHandler> cache = new ConcurrentHashMap<String, JSONPropertiesHandler>(128);
	
	/** the JsonValue containing the object tree. */
	private JsonValue jsonValue;
	
	/** file path on disk. */
	private final String configPath;

	/** timestamp of the last modification date. */
	private long lastModified;
	
	/** true if the json file exists on disk, false if not. */
	private boolean exists = false;
	
	/**
	 * @param configPath The relative path to the json file.
	 */
	public JSONPropertiesHandler (String configPath) {
		this(configPath, false);
	}
	
	/**
	 * @param configPath The relative path to the json file.
	 * @param ignoreCache Always loads the file from disk if true. If false the cached version will be used if available.
	 */
	public JSONPropertiesHandler(String configPath, boolean ignoreCache) {
		
		if(configPath == null) {
			throw new IllegalStateException("The path cannot be null.");
		}
		
		this.configPath = configPath.replace('\\','/');
	
		if(ignoreCache || !cache.containsKey(this.configPath)) {
			
			try {
				
				JsonReader jsonReader = new JsonReader();
				
				File file = new File(this.configPath);
				
				jsonValue = jsonReader.parse(new FileInputStream(file));
				
				if(jsonValue == null) {
					jsonValue = new JsonValue(ValueType.object);
				}
				
				this.lastModified = file.lastModified();
				
				this.exists = true;
				
			} catch (Exception e) {
				
				this.jsonValue = new JsonValue(ValueType.object);
				this.lastModified = -1;
				this.exists = false;
			}
			
		} else {
			
			JSONPropertiesHandler cachedValue = cache.get(this.configPath);
			
			this.jsonValue = cachedValue.jsonValue;
			this.lastModified = cachedValue.lastModified;
			this.exists = cachedValue.exists;
		}

		cache.put(this.configPath, this);
	}
	
	public void save() {
		
		String fileDir = configPath.substring(0, Math.max(configPath.lastIndexOf("/"), 0));
		
		if(fileDir.length() > 0) {
			File directory = new File(fileDir);
			directory.mkdirs();
		}
		
		try {
			
			FileOutputStream writer = new FileOutputStream(configPath);
			
			PrettyPrintSettings settings = new PrettyPrintSettings();
			
			settings.outputType = OutputType.json;
			settings.singleLineColumns = 0;
			settings.wrapNumericArrays = false;
			
			writer.write(jsonValue.prettyPrint(settings).getBytes("utf-8"));
			writer.close();
			
			lastModified = System.currentTimeMillis();
			
			exists = true;
			
		} catch (Exception e) {
			exists = false;
			e.printStackTrace();
		}
	}
	
	public void reload() {

		if(!exists) {
			return;
		}
		
		long lastModifiedFile = new File(this.configPath).lastModified();
		
		if(lastModifiedFile <= this.lastModified) {
			return;
		}
		
		lastModified = lastModifiedFile;
		
		try {
			
			JsonReader jsonReader = new JsonReader();
			jsonValue = jsonReader.parse(new FileInputStream(this.configPath));
			
			if(jsonValue == null) {
				jsonValue = new JsonValue(ValueType.object);
			}
			
			cache.put(configPath, this);
			
			exists = true;
			
		} catch (Exception e) {
			exists = false;
			e.printStackTrace();
		}
	}
	
	public static void clearCache() {
		cache.clear();
	}
	
	public static void reloadCachedValues() {
		for (JSONPropertiesHandler cachedValue : cache.values()) {
			cachedValue.reload();
		}
	}
	
	/** @return true if the json file exists on disk, false if not.*/
	public boolean exists() {
		return exists;
	}
	
	public JsonValue getJsonValue() {
		return jsonValue;
	}

	public void set(JsonValue value) {
		jsonValue = value;
	}
}
