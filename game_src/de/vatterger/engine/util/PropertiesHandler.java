package de.vatterger.engine.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Properties;

public class PropertiesHandler {
	
	private static HashMap<String, Properties> cache = new HashMap<String, Properties>(32);
	
	private static volatile Object cacheLock = new Object();
	
	private Properties properties = null;
	private String configPath = null;
	private boolean exists = false;
	
	public PropertiesHandler(String configPath) {
		if(configPath == null) {
			throw new IllegalStateException("The path cannot be null.");
		}

		this.configPath = configPath.replace('\\','/');
		
		synchronized (cacheLock) {
			if((properties = cache.get(configPath)) == null) {
				try {
					properties = new Properties();
					BufferedInputStream stream = new BufferedInputStream(new FileInputStream(this.configPath));
					properties.load(stream);
					stream.close();
					cache.put(configPath, properties);
					exists = true;
				} catch (Exception e) {
					e.printStackTrace();
					exists = false;
				}
			} else {
				exists = true;
			}
		}
	}
	
	public void save(String comment) {
		File file = new File(configPath);
		File directory = new File(configPath.substring(0, configPath.lastIndexOf("/")));
		directory.mkdirs();
		try {
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
			properties.store(stream, comment);
			stream.close();
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
		synchronized (cacheLock) {
			cache.remove(configPath);
		}
		try {
			properties = new Properties();
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream(configPath));
			properties.load(stream);
			stream.close();
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
	
	public String getString(String name, String defaultValue){
		if(properties.containsKey(name))
			return properties.getProperty(name);
		else
			return defaultValue;
	}
	
	public long getLong(String name, long defaultValue){
		try {
			if(properties.containsKey(name))
				return Long.valueOf(properties.getProperty(name));
			else
				return defaultValue;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IllegalStateException("Field "+name+" cannot be interpreted as a long value");
		}
	}
	
	public int getInt(String name, int defaultValue){
		try {
			if(properties.containsKey(name))
				return Integer.valueOf(properties.getProperty(name));
			else
				return defaultValue;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Field "+name+" cannot be interpreted as an integer value");
		}
	}

	public float getFloat(String name, float defaultValue){
		try {
			if(properties.containsKey(name))
				return Float.valueOf(properties.getProperty(name));
			else
				return defaultValue;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Field "+name+" cannot be interpreted as a float value");
		}
	}

	public double getDouble(String name, double defaultValue){
		try {
			if(properties.containsKey(name))
				return Double.valueOf(properties.getProperty(name));
			else
				return defaultValue;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Field "+name+" cannot be interpreted as a double value");
		}
	}

	public boolean getBoolean(String name, boolean defaultValue){
		try {
			if(properties.containsKey(name))
				return Boolean.valueOf(properties.getProperty(name));
			else
				return defaultValue;
		} catch (Exception e) {
			throw new IllegalStateException("Field "+name+" cannot be interpreted as a boolean value");
		}
	}

	public boolean has(String string) {
		return properties.containsKey(string);
	}
	
	public void setBoolean(String name, boolean value){
		if(name != null)
			properties.setProperty(name, String.valueOf(value));
	}

	public void setInt(String name, int value){
		if(name != null)
			properties.setProperty(name, String.valueOf(value));
	}

	public void setFloat(String name, float value){
		if(name != null)
			properties.setProperty(name, String.valueOf(value));
	}
	
	public void setDouble(String name, Double value){
		if(name != null)
			properties.setProperty(name, String.valueOf(value));
	}
	
	public void setLong(String name, long value){
		if(name != null)
			properties.setProperty(name, String.valueOf(value));
	}
	
	public void setString(String name, String value){
		if(name != null)
			properties.setProperty(name, value);
	}
}
