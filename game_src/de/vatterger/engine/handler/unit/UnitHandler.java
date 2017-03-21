package de.vatterger.engine.handler.unit;

import org.lwjgl.opengl.GL11;

import com.artemis.World;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.PropertiesHandler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.SpriteDrawMode;
import de.vatterger.game.components.gameobject.SpriteID;
import de.vatterger.game.components.gameobject.SpriteLayer;
import de.vatterger.game.components.gameobject.TracerTarget;
import de.vatterger.game.components.gameobject.Turret;
import de.vatterger.game.components.gameobject.Turrets;
import de.vatterger.game.components.gameobject.Velocity;

public class UnitHandler {
	
	private UnitHandler() {}
	
	public static int createTank(String name, Vector3 position, World world) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/tank/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int turrets = properties.getInt("turrets", 0);

		int hullId = AtlasHandler.getIdFromName(name+"_h");

		int e = world.create();
		
		Turrets turretsComponent = new Turrets(turrets);
		
		float hullRotation = MathUtils.random(360f);
		
		world.edit(e)
		.add(new AbsolutePosition(position.x, position.y, position.z))
		.add(new AbsoluteRotation(hullRotation))
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
			.add(new AbsolutePosition())
			.add(new AbsoluteRotation())
			.add(new Attached(e, offset, 0f))
			.add(new SpriteID(turretId))
			.add(new SpriteLayer(SpriteLayer.OBJECTS1))
			.add(new Turret())
			.add(new CullDistance(32));
		}
		
		
		return e;
	}

	public static int createInfatry(String name, Vector3 position, World world) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/infantry/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int spriteID = AtlasHandler.getIdFromName(name+"_p0");
		
		int e = world.create();
		
		world.edit(e)
		.add(new AbsolutePosition(position.x, position.y, position.z))
		.add(new AbsoluteRotation())
		.add(new SpriteID(spriteID))
		.add(new SpriteLayer(SpriteLayer.OBJECTS0))
		.add(new CullDistance(16));

		return e;
	}

	public static int createGroundTile(String name, Vector3 position, World world) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/misc/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int spriteID = AtlasHandler.getIdFromName(name);
		
		int e = world.create();
		
		world.edit(e)
		.add(new AbsolutePosition(position.x, position.y, position.z))
		.add(new SpriteID(spriteID))
		.add(new SpriteLayer(SpriteLayer.GROUND0))
		.add(new CullDistance(Metrics.sssm));

		return e;
	}

	public static int createStaticObject(String name, Vector3 position, World world) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/misc/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int spriteID = AtlasHandler.getIdFromName(name);
		
		int e = world.create();
		
		world.edit(e)
		.add(new AbsolutePosition(position.x, position.y, position.z))
		.add(new SpriteID(spriteID))
		.add(new SpriteLayer(SpriteLayer.OBJECTS0))
		.add(new CullDistance(Metrics.sssm));

		return e;
	}

	public static int createStaticObject(String name, Vector3 position, int layer, World world) {
		int e = createStaticObject(name, position, world);
		
		world.edit(e).add(new SpriteLayer(layer));

		return e;
	}

	public static int createTracer(String name, Vector3 position, Vector3 target, Vector3 velocity, float angle, World world) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/fx/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int spriteID = AtlasHandler.getIdFromName(name);
		
		int e = world.create();
		
		world.edit(e)
		.add(new AbsolutePosition(position.x, position.y, position.z))
		.add(new AbsoluteRotation(angle))
		.add(new Velocity(velocity.x, velocity.y, velocity.z))
		.add(new TracerTarget(target.x, target.y, target.z).setSpread(position.dst(target)*0.005f))
		.add(new SpriteID(spriteID))
		.add(new SpriteDrawMode(GL11.GL_ONE, GL11.GL_ONE))
		.add(new SpriteLayer(SpriteLayer.OBJECTS1))
		.add(new CullDistance(Metrics.sssm));

		return e;
	}
}
