package de.vatterger.engine.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Properties;

public class PropertiesHandler {
	
	private Properties properties = null;
	private boolean success = false;
	
	
	public PropertiesHandler(String configPath) {
		if(configPath == null) {
			throw new IllegalStateException("the path cannot be null.");
		}
		
		properties = new Properties();
		
		try {
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream(configPath));
			properties.load(stream);
			stream.close();
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean exists() {
		return success;
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
}
