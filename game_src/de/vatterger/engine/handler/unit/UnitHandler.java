package de.vatterger.engine.handler.unit;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import com.artemis.World;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.PropertiesHandler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.SpriteDrawMode;
import de.vatterger.game.components.gameobject.SpriteID;
import de.vatterger.game.components.gameobject.SpriteLayer;
import de.vatterger.game.components.gameobject.TerrainHeightField;
import de.vatterger.game.components.gameobject.TracerTarget;
import de.vatterger.game.components.gameobject.Turret;
import de.vatterger.game.components.gameobject.Turrets;
import de.vatterger.game.components.gameobject.Velocity;

public class UnitHandler {
	
	private UnitHandler() {}
	
	/**
	 * Adds a tank unit to the {@link World}
	 * @param name The type name of unit
	 * @param position The world position of this unit
	 * @param world The world to add this unit to
	 * @return The entity id or if failed -1
	 */
	public static int createTank(String name, Vector3 position, World world) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/tank/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int turrets = properties.getInt("turrets", 0);
		
		HashMap<String, Integer> nameToIdMap = new HashMap<String, Integer>(turrets + 1);
		
		int hullId = AtlasHandler.getIdFromName(properties.getString("hull_sprite", name + "_h"));
		
		int e_hull = world.create();
		nameToIdMap.put("hull", e_hull);
		
		Turrets turretsComponent = new Turrets(turrets);
		
		float hullRotation = MathUtils.random(360f);
		
		world.edit(e_hull)
		.add(new AbsolutePosition(position.x, position.y, position.z))
		.add(new AbsoluteRotation(hullRotation))
		.add(new SpriteID(hullId))
		.add(new SpriteLayer(SpriteLayer.OBJECTS0))
		.add(new CullDistance(
				properties.getFloat("cullradius", 32f),
				properties.getFloat("cullradius_offset_x", 0f),
				properties.getFloat("cullradius_offset_y", 0f)))
		.add(turretsComponent);
		
		for (int i = 0; i < turrets; i++) {
			
			int turretId = AtlasHandler.getIdFromName(properties.getString("turret_" + i + "_sprite", name + "_t" + i));
			int flashId = AtlasHandler.getIdFromName(properties.getString("turret_" + i + "_flash_sprite", "flash_big"));
			
			Vector3 offset = new Vector3(
					properties.getFloat("turret_"+i+"_x", 0f),
					properties.getFloat("turret_"+i+"_y", 0f),
					properties.getFloat("turret_"+i+"_z", 0f)
			);
			
			int e_turret = world.create();
			nameToIdMap.put("turret_" + i, e_turret);
			String s_turret_parent = properties.getString("turret_" + i + "_parent", "hull");
			int e_turret_parent = nameToIdMap.getOrDefault(s_turret_parent, e_hull);
			
			turretsComponent.turretIds[i] = e_turret;
			
			world.edit(e_turret)
			.add(new AbsolutePosition())
			.add(new AbsoluteRotation())
			.add(new Attached(e_turret_parent, offset, 0f))
			.add(new SpriteID(turretId))
			.add(new SpriteLayer(SpriteLayer.OBJECTS0))
			.add(new Turret())
			.add(new CullDistance(properties.getFloat("cullradius", 32f)*2f));
			
			
			int e_flash_turret = world.create();
			
			world.edit(e_flash_turret)
			.add(new AbsolutePosition())
			.add(new AbsoluteRotation())
			.add(new Attached(e_turret, new Vector3(
					properties.getFloat("turret_" + i + "_flash_offset_x", 0f),
					properties.getFloat("turret_" + i + "_flash_offset_y", 2f),
					properties.getFloat("turret_" + i + "_flash_offset_z", 0f))
					))
			.add(new SpriteID(flashId))
			.add(new SpriteDrawMode(GL11.GL_ONE, GL11.GL_ONE))
			.add(new SpriteLayer(SpriteLayer.OBJECTS1))
			.add(new CullDistance(16f));
		}
		
		return e_hull;
	}
	
	/**
	 * Adds an infantry unit to the {@link World}
	 * @param name The type name of unit
	 * @param position The world position of this unit
	 * @param world The world to add this unit to
	 * @return The entity id or if failed -1
	 */
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
		.add(new CullDistance(
				properties.getFloat("cullradius", 1f),
				properties.getFloat("cullradius_offset_x", 0f),
				properties.getFloat("cullradius_offset_y", 0f))
		);
		
		return e;
	}
	
	public static int createGroundTile(String name, Vector3 position, World world) {
		return createGroundTile(name, position, SpriteLayer.GROUND0, world);
	}
	
	public static int createGroundTile(String name, Vector3 position, int layer, World world) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/misc/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int spriteID = AtlasHandler.getIdFromName(name);
		
		int e = world.create();
		
		world.edit(e)
		.add(new AbsolutePosition(position.x, position.y, position.z))
		.add(new SpriteID(spriteID))
		.add(new SpriteLayer(layer))
		.add(new CullDistance(Metrics.sssm))
		.add(new SpriteDrawMode(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA));
		
		return e;
	}
	
	public static int createRandomTerrainTile(Vector3 v, World world) {
		float heightField[][] = new float[11][11];
		
		for (int i = 0; i < heightField.length; i++) {
			for (int j = 0; j < heightField[0].length; j++) {
				heightField[i][j] = MathUtils.random(0f,1f);
			}
		}
		
		return createTerrainTile(heightField, v, world);
	}
	
	public static int createTerrainTile(float heightField[][], Vector3 position, World world) {
		int e = world.create();
		
		float terrainSizeX = 25f*(heightField[0].length	- 1);
		float terrainSizeY = 25f*(heightField.length	- 1);
		
		world.edit(e)
		.add(new AbsolutePosition(position.x, position.y, position.z))
		.add(new TerrainHeightField(heightField,50f,1f))
		.add(new CullDistance(Math.max(terrainSizeX,terrainSizeY),terrainSizeX, terrainSizeY));

		return e;
	}

	/**
	 * Adds a static object to the {@link World}
	 * @param name The type name of object
	 * @param position The world position of this object
	 * @param world The world to add this object to
	 * @return The entity id or if failed -1
	 */
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
		.add(new CullDistance(
				properties.getFloat("cullradius", 256f),
				properties.getFloat("cullradius_offset_x", 0f),
				properties.getFloat("cullradius_offset_y", 0f))
		);

		return e;
	}

	/**
	 * Adds a house to the {@link World}
	 * @param name The type name of object
	 * @param position The world position of this object
	 * @param world The world to add this object to
	 * @return The entity id or if failed -1
	 */
	public static int createHouse(String name, Vector3 position, World world) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/object/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int spriteID = AtlasHandler.getIdFromName(name);
		
		int e = world.create();
		
		world.edit(e)
		.add(new AbsolutePosition(position.x, position.y, position.z))
		.add(new SpriteID(spriteID))
		.add(new SpriteLayer(SpriteLayer.OBJECTS0))
		.add(new CullDistance(
				properties.getFloat("cullradius", 256f),
				properties.getFloat("cullradius_offset_x", 0f),
				properties.getFloat("cullradius_offset_y", 0f))
		);

		return e;
	}

	/**
	 * Adds a static object to the {@link World}
	 * @param name The type name of object
	 * @param position The world position of this object
	 * @param layer The height layer of this object (->render-order)
	 * @param world The world to add this object to
	 * @return The entity id or if failed -1
	 */
	public static int createStaticObject(String name, Vector3 position, int layer, World world) {
		int e = createStaticObject(name, position, world);
		
		world.edit(e).add(new SpriteLayer(layer));

		return e;
	}

	/**
	 * Adds a tracer effect to the {@link World}
	 * @param name The type name of effect
	 * @param position The initial world position of this effect
	 * @param target The target position of this tracer
	 * @param world The world to add this object to
	 * @return The entity id or if failed -1
	 */
	public static int createTracer(String name, Vector3 position, Vector3 target, Vector3 velocity, World world) {
		PropertiesHandler properties = new PropertiesHandler("assets/data/fx/"+name+".u");
		
		if(!properties.exists())
			return -1;
		
		int spriteID = AtlasHandler.getIdFromName(name);
		
		int e = world.create();
		
		float angle = Math2D.atan2d(target.y-position.y, target.x-position.x);
		
		world.edit(e)
		.add(new AbsolutePosition(position.x, position.y, position.z))
		.add(new AbsoluteRotation(angle))
		.add(new Velocity(velocity.x, velocity.y, velocity.z))
		.add(new TracerTarget(target.x, target.y, target.z).setSpread(position.dst(target)*0.005f))
		.add(new SpriteID(spriteID))
		.add(new SpriteDrawMode(GL11.GL_ONE, GL11.GL_ONE))
		.add(new SpriteLayer(SpriteLayer.OBJECTS1))
		.add(new CullDistance(
				properties.getFloat("cullradius", 64f),
				properties.getFloat("cullradius_offset_x", 0f),
				properties.getFloat("cullradius_offset_y", 0f))
		);

		return e;
	}
}
