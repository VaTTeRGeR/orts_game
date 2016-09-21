package de.vatterger.tests;

import de.vatterger.engine.util.PropertiesHandler;

public class PropertiesTest {
	public static void main(String[] args) {
		PropertiesHandler handler = new PropertiesHandler("tests/p.properties");
		handler.setString("saltyhash", "okdokey");
		handler.save("COMMENT");
	}
}
