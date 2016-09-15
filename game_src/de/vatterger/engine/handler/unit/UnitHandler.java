package de.vatterger.engine.handler.unit;

import com.artemis.World;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.PropertiesHandler;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.SpriteID;
import de.vatterger.game.components.gameobject.SpriteLayer;
import de.vatterger.game.components.gameobject.SpriteRotation;

public class UnitHandler {
	
	private static World world;
	
	private UnitHandler() {}
	
	public static void setWorld(World world){
		UnitHandler.world = world;
	}
	
	public static int createTank(String name, Vector3 position) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/tank/"+name+".u");
		if(!properties.exists())
			return -1;
		int turrets = properties.getInt("turrets", 0);
		int[] spriteIDs = new int[turrets+1];
		Vector3[] offsets = new Vector3[turrets+1];
		
		spriteIDs[0] = AtlasHandler.getIdFromName(name+"_h");
		for (int i = 0; i < turrets; i++) {
			spriteIDs[i+1] = AtlasHandler.getIdFromName(name + "_t" + i);
			offsets[i+1] = new Vector3(
					properties.getFloat("x"+i, 0f),
					properties.getFloat("y"+i, 0f),
					properties.getFloat("z"+i, 0f));
		}
		int e = world.create();
		world.edit(e)
		.add(new Position(position.x, position.y, position.z))
		.add(new SpriteRotation(new float[turrets+1]))
		.add(new SpriteID(spriteIDs, offsets))
		.add(new SpriteLayer(1));
		
		return e;
	}

	public static int createInfatry(String name, Vector3 position) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/infantry/"+name+".u");
		if(!properties.exists())
			return -1;
		int[] spriteIDs = new int[1];
		Vector3[] offsets = new Vector3[1];
		
		spriteIDs[0] = AtlasHandler.getIdFromName(name+"_p0");
		
		int e = world.create();
		world.edit(e)
		.add(new Position(position.x, position.y, position.z))
		.add(new SpriteRotation(new float[1]))
		.add(new SpriteID(spriteIDs, offsets))
		.add(new SpriteLayer(1));

		return e;
	}

	public static int createGroundTile(String name, Vector3 position) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/misc/"+name+".u");
		if(!properties.exists())
			return -1;
		int[] spriteIDs = new int[1];
		Vector3[] offsets = new Vector3[1];
		
		spriteIDs[0] = AtlasHandler.getIdFromName(name);
		
		int e = world.create();
		world.edit(e)
		.add(new Position(position.x, position.y, position.z))
		.add(new SpriteRotation(new float[1]))
		.add(new SpriteID(spriteIDs, offsets))
		.add(new SpriteLayer(0));

		return e;
	}
}
