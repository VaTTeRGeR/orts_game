package de.vatterger.engine.handler.asset;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

import de.vatterger.engine.handler.asset.AssetPathFinder.AssetPath;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.PropertiesHandler;

/**
 * @author Florian
 *
 */
public final class AtlasHandler {
	
	private static TextureAtlas atlas;
	private static HashMap<String, Integer> ntim; //name to id mapping
	private static ArrayList<String> itnm; //id to name mapping
	private static ArrayList<Array<Sprite>> spriteStore;
	private static int counter;

	
	static {
		dispose();
		
		atlas = new TextureAtlas("atlas/packfile.atlas");
		
		for (AssetPath path : AssetPathFinder.searchForAssets(".u", "data/tank")) {
			AtlasHandler.registerTankSprites(path.name);
		}

		for (AssetPath path : AssetPathFinder.searchForAssets(".u", "data/infantry")) {
			AtlasHandler.registerInfantrySprites(path.name);
		}

		for (AssetPath path : AssetPathFinder.searchForAssets(".u", "data/misc")) {
			AtlasHandler.registerMiscSprites(path.name);
		}

		for (AssetPath path : AssetPathFinder.searchForAssets(".u", "data/fx")) {
			AtlasHandler.registerMiscSprites(path.name);
		}
	}
	
	/**
	 * Adds a sprite to the sprite<->id<->name mapping
	 * @param name The name the sprites are registered under.
	 * @param sprites The sprites that get registered, do not modify the Array anymore
	 */
	private static void addToStore(String name, Array<Sprite> sprites){
		correctSize(sprites);
		spriteStore.add(counter, sprites);
		ntim.put(name, counter);
		itnm.add(counter, name);
		counter ++;
	}
	
	/**
	 * Sets the scaling of the sprites for correct drawing.
	 * @param sprites The sprites that need to get adjusted.
	 */
	private static void correctSize(Array<Sprite> sprites) {
		for (int i = 0; i < sprites.size; i++) {
			Sprite sprite = sprites.get(i);
			sprite.setSize(sprite.getWidth() * Metrics.mpp, sprite.getHeight() * Metrics.mpp);
		}
	}
	
	/**
	 * Registers The turret and hull sprites of the specified tank
	 * @param name The name of the tank
	 */
	public static void registerTankSprites(String name) {
		PropertiesHandler p = new PropertiesHandler("assets/data/tank/"+name+".u");
		final String hullName = name + "_h";
		addToStore(hullName, atlas.createSprites(hullName));
		for (int i = 0; i < p.getInt("turrets", 0); i++) {
			final String turretName = name + "_t" + i;
			addToStore(turretName, atlas.createSprites(turretName));
		}
	}
	
	/**
	 * Registers The stance sprites of the specified unit
	 * @param name The name of the unit
	 */
	public static void registerInfantrySprites(String name) {
		final String n0 = name + "_p0";
		addToStore(n0, atlas.createSprites(n0));
	}
	
	/**
	 * Registers The sprites of the specified object
	 * @param name The name of the object
	 */
	public static void registerMiscSprites(String name) {
		addToStore(name, atlas.createSprites(name));
	}
	
	/**
	 * @param name The name of the sprite(set)
	 * @return The id of the sprite(set)
	 */
	public static int getIdFromName(String name) {
		return ntim.getOrDefault(name, -1);
	}
	
	/**
	 *@return true if the Sprite has exactly 8 different rotated versions.
	 */
	public static boolean isEightAngleSprite(int id) {
		return spriteStore.get(id).size == 8;
	}
	
	/**
	 *@return true if the Sprite has exactly 16 different rotated versions.
	 */
	public static boolean isSixteenAngleSprite(int id) {
		return spriteStore.get(id).size == 16;
	}
	
	/**
	 * Returns a cached Sprite, it's state needs to be set before usage, previous state is not cleared by the AtlasHandler.
	 * @param id The Identifier of the Sprite, name to id is performed with {@link AtlasHandler#getIdFromName}
	 */
	public static Sprite getSharedSpriteFromId(int id) {
		return spriteStore.get(id).first();
	}
	
	public static Sprite getSharedSpriteFromId(int id, int frame) {
		Array<Sprite> sprites = spriteStore.get(id);
		return sprites.get(frame%sprites.size);
	}
	
	public static Array<Sprite> getSharedSpritesFromId(int id) {
		return spriteStore.get(id);
	}
	
	/**
	 * Disposes the Atlas and clears the internal state.
	 */
	public static void dispose() {
		if(atlas != null)
			atlas.dispose();

		atlas = null;
		
		ntim		= new HashMap<String, Integer>();
		itnm		= new ArrayList<String>();
		spriteStore	= new ArrayList<Array<Sprite>>();
		
		counter = 0;
	}
}
