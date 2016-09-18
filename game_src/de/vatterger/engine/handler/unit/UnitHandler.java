package de.vatterger.engine.handler.unit;

import com.artemis.World;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.PropertiesHandler;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.SpriteID;
import de.vatterger.game.components.gameobject.SpriteLayer;
import de.vatterger.game.components.gameobject.SpriteRotation;
import de.vatterger.game.components.gameobject.Turrets;

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
		
		int hullId = AtlasHandler.getIdFromName(name+"_h");

		int e = world.create();
		
		Turrets turretsComponent = new Turrets(turrets);
		
		world.edit(e)
		.add(new Position(position.x, position.y, position.z))
		.add(new SpriteRotation())
		.add(new SpriteID(hullId))
		.add(new SpriteLayer(SpriteLayer.OBJECTS0))
		.add(new CullDistance(32))
		.add(turretsComponent);
		
		for (int i = 0; i < turrets; i++) {
			int turretId = AtlasHandler.getIdFromName(name + "_t" + i);
			Vector3 offset = new Vector3(
					properties.getFloat("x"+i, 0f),
					properties.getFloat("y"+i, 0f),
					properties.getFloat("z"+i, 0f));

			int te = world.create();
			
			turretsComponent.turretIds[i] = te;
			
			world.edit(te)
			.add(new Position())
			.add(new Attached(e, offset))
			.add(new SpriteRotation())
			.add(new SpriteID(turretId))
			.add(new SpriteLayer(SpriteLayer.OBJECTS1))
			.add(new CullDistance(32));
		}
		
		
		return e;
	}

	public static int createInfatry(String name, Vector3 position) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/infantry/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int spriteID = AtlasHandler.getIdFromName(name+"_p0");
		
		int e = world.create();
		
		world.edit(e)
		.add(new Position(position.x, position.y, position.z))
		.add(new SpriteRotation())
		.add(new SpriteID(spriteID))
		.add(new SpriteLayer(SpriteLayer.OBJECTS0))
		.add(new CullDistance(16));

		return e;
	}

	public static int createGroundTile(String name, Vector3 position) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/misc/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int spriteID = AtlasHandler.getIdFromName(name);
		
		int e = world.create();
		
		world.edit(e)
		.add(new Position(position.x, position.y, position.z))
		.add(new SpriteID(spriteID))
		.add(new SpriteLayer(SpriteLayer.GROUND))
		.add(new CullDistance(Metrics.sssm));

		return e;
	}

	public static int createStaticObject(String name, Vector3 position) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/misc/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int spriteID = AtlasHandler.getIdFromName(name);
		
		int e = world.create();
		
		world.edit(e)
		.add(new Position(position.x, position.y, position.z))
		.add(new SpriteID(spriteID))
		.add(new SpriteLayer(SpriteLayer.OBJECTS0))
		.add(new CullDistance(Metrics.sssm));

		return e;
	}

	public static int createStaticObject(String name, Vector3 position, float angle) {
		int e = createStaticObject(name, position);
		if(e != -1)
			world.edit(e).add(new SpriteRotation(angle));
		return e;
	}

	public static int createFlash(String name, Vector3 position, float angle) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/fx/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int spriteID = AtlasHandler.getIdFromName(name);
		
		int e = world.create();
		
		world.edit(e)
		.add(new Position(position.x, position.y, position.z))
		.add(new SpriteID(spriteID))
		.add(new SpriteLayer(SpriteLayer.OBJECTS2))
		.add(new CullDistance(Metrics.sssm))
		.add(new SpriteRotation(angle));

		return e;
	}
}
