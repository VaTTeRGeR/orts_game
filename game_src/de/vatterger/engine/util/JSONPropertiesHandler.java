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
	
	private static final ConcurrentHashMap<String, JsonValue> cache = new ConcurrentHashMap<String, JsonValue>(128);
	
	private JsonValue	properties = null;
	private String		configPath = null;
	
	/***/
	private boolean exists = false;
	
	public JSONPropertiesHandler(String configPath) {
		
		if(configPath == null) {
			throw new IllegalStateException("The path cannot be null.");
		}
		
		this.configPath = configPath.replace('\\','/');
	
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
			exists = false;
			e.printStackTrace();
		}
	}
	
	public static void clearCache() {
		cache.clear();
	}
	
	public void reload() {

		try {
			
			JsonReader jsonReader = new JsonReader();
			properties = jsonReader.parse(new FileInputStream(this.configPath));
			
			if(properties == null) {
				properties = new JsonValue(ValueType.object);
			}
			
			cache.put(configPath, properties);
			
			exists = true;
			
		} catch (Exception e) {
			exists = false;
			e.printStackTrace();
		}
	}
	
	public boolean exists() {
		return exists;
	}
	
	public JsonValue get() {
		return properties;
	}
}
