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

/**
 * Encapsulates a json file to provide a simple utility for saving and loading from disk.
 * The json files are cached such that they get loaded from disk only once,
 * call {@link #reload()} or {@link #reloadCachedValues()} to circumvent the cache.<p>
 * Call {@link #getJsonValue()} to get the {@link JsonValue} respresentation of the json file.
 * @author Florian
 */
public class JSONPropertiesHandler {
	
	private static final ConcurrentHashMap<String, JSONPropertiesHandler> cache = new ConcurrentHashMap<String, JSONPropertiesHandler>(128);
	
	/** the JsonValue containing the object tree. */
	private JsonValue jsonValue;
	
	/** file path on disk. */
	private final String configPath;

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
				
				this.exists = true;
				
			} catch (Exception e) {
				
				this.jsonValue = new JsonValue(ValueType.object);
				this.exists = false;
			}
			
		} else {
			
			JSONPropertiesHandler cachedValue = cache.get(this.configPath);
			
			this.jsonValue = cachedValue.jsonValue;
			this.exists = cachedValue.exists;
		}

		cache.put(this.configPath, this);
	}
	
	/**
	 * Writes the {@link JsonValue} that is stored in this Handler to disk.
	 */
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
			
			exists = true;
			
		} catch (Exception e) {
			exists = false;
			e.printStackTrace();
		}
	}
	
	/**
	 * Reloads the internal {@link JsonValue} from disk.
	 */
	public void reload() {

		if(!exists) {
			return;
		}
		
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
	
	/**
	 * Calls {@link #reload()} for every {@link JsonValue} that is currently cached.
	 */
	public static void reloadCachedValues() {
		for (JSONPropertiesHandler cachedValue : cache.values()) {
			cachedValue.reload();
		}
	}
	
	/**
	 * Clears out the internal cache. All {@link JsonValue}s will be loaded from disk first when accessed now.
	 */
	public static void clearCache() {
		cache.clear();
	}
	
	/**
	 * @return true if the json file exists on disk, false if not.
	 */
	public boolean exists() {
		return exists;
	}
	
	/**
	 * @return The internal {@link JsonValue}.
	 */
	public JsonValue getJsonValue() {
		return jsonValue;
	}

	/**
	 * Sets the internal {@link JsonValue} to the provided value.
	 * @param value the {@link JsonValue} that should be stored in this Handler.
	 */
	public void set(JsonValue value) {
		jsonValue = value;
	}
}
