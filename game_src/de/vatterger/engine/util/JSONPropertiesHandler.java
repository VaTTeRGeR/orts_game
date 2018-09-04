package de.vatterger.engine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.PrettyPrintSettings;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class JSONPropertiesHandler {
	
	private static HashMap<String, JsonValue> cache = new HashMap<String, JsonValue>(32);
	
	private static final Object cacheLock = new Object();
	
	private JsonValue properties = null;
	private String configPath = null;
	private boolean exists = false;
	
	public JSONPropertiesHandler(String configPath) {
		if(configPath == null) {
			throw new IllegalStateException("The path cannot be null.");
		}
		
		this.configPath = configPath.replace('\\','/');
		
		synchronized (cacheLock) {
			if((properties = cache.get(configPath)) == null) {
				try {
					JsonReader jsonReader = new JsonReader();
					properties = jsonReader.parse(new FileInputStream(this.configPath));
					
					if(properties == null) {
						properties = new JsonValue(ValueType.object);
					}
					
					cache.put(configPath, properties);
					exists = true;
				} catch (Exception e) {
					properties = new JsonValue(ValueType.object);
					exists = false;
				}
			} else {
				exists = true;
			}
		}
	}
	
	public void save() {
		File directory = new File(configPath.substring(0, configPath.lastIndexOf("/")));
		directory.mkdirs();
		FileOutputStream writer;
		try {
			writer = new FileOutputStream(configPath);
			
			PrettyPrintSettings settings = new PrettyPrintSettings();
			settings.outputType = OutputType.minimal;
			settings.singleLineColumns = 0;
			settings.wrapNumericArrays = false;
			
			writer.write(properties.prettyPrint(settings).getBytes("UTF-8"));
			writer.close();
			
			exists = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void clearCache() {
		synchronized (cacheLock) {
			cache.clear();
		}
	}
	
	public void reload() {
		try {
			JsonReader jsonReader = new JsonReader();
			properties = jsonReader.parse(new FileInputStream(this.configPath));
			
			if(properties == null) {
				properties = new JsonValue(ValueType.object);
			}
			
			synchronized (cacheLock) {
				cache.put(configPath, properties);
			}
		} catch (Exception e) {
			exists = false;
			e.printStackTrace();
		}
		exists = true;
	}
	
	public boolean exists() {
		return exists;
	}
	
	public JsonValue get() {
		return properties;
	}
}
