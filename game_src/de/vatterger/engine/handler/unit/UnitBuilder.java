package de.vatterger.engine.handler.unit;

import java.util.HashMap;

import com.artemis.EntityEdit;
import com.artemis.World;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.JSONPropertiesHandler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.Attached;
import de.vatterger.game.components.gameobject.CollisionRadius;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.CullMetersPerPixel;
import de.vatterger.game.components.gameobject.CullingParent;
import de.vatterger.game.components.gameobject.SpriteID;
import de.vatterger.game.components.gameobject.SpriteLayer;
import de.vatterger.game.components.gameobject.Turret;

public class UnitBuilder {
	
	private final JSONPropertiesHandler json;

	private HashMap<String, Integer> nameIdMap = new HashMap<String, Integer>(4);
	private Vector3 position = null;
	private World world = null;
	
	public UnitBuilder(String path) {
		json = new JSONPropertiesHandler("assets/data/" + path + ".json");
	}
	
	/**
	 * Adds a tank unit to the {@link World}
	 * @param name The type name of unit
	 * @param position The world position of this unit
	 * @param world The world to add this unit to
	 * @return The entity id or if failed -1
	 */
	public int spawnUnit(Vector3 position, World world) {
		
		this.position = position;
		this.world = world;
		
		if(!json.exists()) {
			return -1;
		}
		
		nameIdMap.clear();
		
		final JsonValue root = json.getJsonValue();
		
		// Create Entities
		JsonValue nextEntity = root.child();
		while (nextEntity != null) {
			
			//System.out.println("Creating EntityIds for " + nextEntity.name);
			
			nameIdMap.put(nextEntity.name, world.create());
			
			nextEntity = nextEntity.next();
		}

		// Create Components
		nextEntity = root.child();
		while (nextEntity != null) {
			
			//System.out.println("Creating Components for " + nextEntity.name);
			
			buildEntity(nextEntity);
			
			nextEntity = nextEntity.next();
		}
		
		return nameIdMap.get(root.child().name);
	}

	private void buildEntity (JsonValue entityJson) {
		
		final int entityId = nameIdMap.get(entityJson.name);
		final EntityEdit entityEdit = world.edit(entityId);
		
		JsonValue nextComponent = entityJson.child();
		while (nextComponent != null) {
			
			//System.out.println("  Adding Component: " + nextComponent.name);
			
			buildComponent(entityEdit, nextComponent, nameIdMap, world);
			
			nextComponent = nextComponent.next();
		}
	}

	private void buildComponent (EntityEdit entityEdit, JsonValue nextComponent, HashMap<String, Integer> nameIdMap, World world) {
		
		switch (nextComponent.name) {
		
		case "position":
			
			AbsolutePosition pos = new AbsolutePosition();
			
			if(nextComponent.getBoolean("setToSpawnPosition", false)) {
				pos.position.set(position);
			} else {
				pos.position.set(
					nextComponent.getFloat("x", 0f),
					nextComponent.getFloat("y", 0f),
					nextComponent.getFloat("z", 0f));
			}
			
			entityEdit.add(pos);
			
			break;
		
			
		case "rotation":
			entityEdit.add(new AbsoluteRotation());
			break;
		
			
		case "sprite":
			entityEdit.add(new SpriteID(AtlasHandler.getIdFromName(nextComponent.getString("name", ""))));
			entityEdit.add(new SpriteLayer(nextComponent.getInt("layer", SpriteLayer.OBJECTS0)));
			break;

			
		case "parent":
			
			final int parentId = nameIdMap.get(nextComponent.getString("name"));
			
			entityEdit.add(new Attached(parentId,
				new Vector3(
					nextComponent.getFloat("offset_x", 0f),
					nextComponent.getFloat("offset_y", 0f),
					nextComponent.getFloat("offset_z", 0f)),
				nextComponent.getFloat("offset_r", 0f)));
			
			if(nextComponent.getBoolean("cullParent", true)) {
				entityEdit.add(new CullingParent(parentId));
			}
			break;
			
			
		case "cull_mpp":
			entityEdit.add(new CullMetersPerPixel(nextComponent.getFloat("mpp", 3f)));
			break;
			
			
		case "cull_radius":
			entityEdit.add(new CullDistance(
				nextComponent.getFloat("radius", 20f),
				nextComponent.getFloat("offset_x", 0f),
				nextComponent.getFloat("offset_y", 0f)));
			break;
			
		case "collision_radius":
			entityEdit.add(new CollisionRadius(
				nextComponent.getFloat("radius", 0.5f),
				nextComponent.getFloat("offset_x", 0f),
				nextComponent.getFloat("offset_y", 0f)));
			break;
			
		case "turret":
			entityEdit.add(new Turret(
				nextComponent.getFloat("angle_min", 0f),
				nextComponent.getFloat("angle_max", 360f)));
			break;
			
			
		default:
			break;
		}
	}
}
