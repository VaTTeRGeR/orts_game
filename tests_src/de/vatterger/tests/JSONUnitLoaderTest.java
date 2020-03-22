package de.vatterger.tests;

import java.io.FileNotFoundException;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import de.vatterger.engine.util.JSONPropertiesHandler;

public class JSONUnitLoaderTest {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		JSONPropertiesHandler handler = new JSONPropertiesHandler("assets/data/tank/pz6h.json");
		
		JsonValue value = handler.getJsonValue();
		
		System.out.println(value.prettyPrint(OutputType.minimal, 0));
		
		handler.save();
		
		/*JsonValue comment = value.get("comment");
		
		if(comment != null) {
			System.out.println(value.get("comment").asString());
		} else {
			System.out.println("No comment...");
			comment = new JsonValue("THIS IS COMMENT @ " + new Date().toString());
			value.addChild("comment", comment);
			
			JsonValue child0 = new JsonValue(ValueType.object);
			child0.addChild("child0_bool", new JsonValue(true));
			
			value.addChild("child0", child0);
		}
		
		
		handler.save();
		handler.reload();
		
		value = handler.getJsonValue();
		
		System.out.println(value.get("comment").asString());*/
	}

}
