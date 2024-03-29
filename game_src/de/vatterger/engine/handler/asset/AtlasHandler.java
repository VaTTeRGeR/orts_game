package de.vatterger.engine.handler.asset;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

import de.vatterger.engine.handler.asset.AssetPathFinder.AssetPath;
import de.vatterger.engine.util.Metrics;

/**
 * @author Florian
 *
 */
public final class AtlasHandler {
	
	private static ArrayList<TextureAtlas> atlasStore;

	private static ArrayList<Array<Sprite>> spriteStore;
	
	private static HashMap<String, Integer> ntim; //name to id mapping
	private static ArrayList<String> itnm; //id to name mapping

	private static int counter;

	
	static {
		
		dispose();
		
		for (AssetPath path : AssetPathFinder.searchForAssets(".atlas","assets/atlas")) {
			
			System.out.println("Found Atlas: " + path.absolutePath);

			TextureAtlas textureAtlas = new TextureAtlas(path.absolutePath);

			atlasStore.add(textureAtlas);

			for (AtlasRegion region : textureAtlas.getRegions()) {
				
				if(getIdFromName(region.name) == -1) {
					
					addToStore(region.name, textureAtlas.createSprites(region.name));
					
				} else if(region.index < 1) {
					
					System.err.println(path.absolutePath + " > " + region.name + "' already exists. New Sprite was discarded.");
					
				}
			}
		}
	}
	
	/**
	 * Adds a sprite to the sprite<->id<->name mapping
	 * @param name The name the sprites are registered under.
	 * @param sprites The sprites that get registered, do not modify the Array anymore
	 */
	private static void addToStore(String name, Array<Sprite> sprites){
		
		correctSizeAndOrigin(sprites);
		
		spriteStore.add(counter, sprites);
		
		ntim.put(name, counter);
		itnm.add(counter, name);
		
		counter++;
	}
	
	/**
	 * Sets the scaling of the sprites for correct drawing.
	 * @param stableSprites The sprites that need to get adjusted.
	 */
	private static void correctSizeAndOrigin(Array<Sprite> stableSprites) {

		for (Sprite sprite : stableSprites) {

			// May contain null elements
			if(sprite == null) continue;
			
			sprite.setScale(Metrics.mpp);

			sprite.setOrigin(sprite.getWidth() / 2f, sprite.getHeight() / 2f);
		}
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
		
		if(atlasStore != null) {
			for (TextureAtlas textureAtlas : atlasStore) {
				textureAtlas.dispose();
			}
		}

		atlasStore = new ArrayList<>();
		
		ntim		= new HashMap<>();
		itnm		= new ArrayList<>();
		spriteStore	= new ArrayList<>();
		
		counter = 0;
	}
}
