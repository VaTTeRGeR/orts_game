package de.vatterger.engine.handler.asset;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

import de.vatterger.engine.util.Metrics;

public final class AtlasHandler {
	
	private static TextureAtlas atlas;
	private static final HashMap<String, Integer> ntim; //name to id mapping
	private static final ArrayList<String> itnm; //id to name mapping
	private static final ArrayList<Array<Sprite>> spriteStore;
	private static int counter;

	static {
		ntim = new HashMap<String, Integer>();
		itnm = new ArrayList<String>();
		spriteStore = new ArrayList<Array<Sprite>>();
		counter = 0;
	}
	
	public static void initialize(float sssm) {
		dispose();
		atlas = new TextureAtlas("atlas/packfile.atlas");
	}
	
	public static void registerTank(String name, int turrets) {
		final String hullName = name + "_h";
		addToStore(hullName, atlas.createSprites(hullName));
		for (int i = 0; i < turrets; i++) {
			final String turretName = name + "_t" + i;
			addToStore(turretName, atlas.createSprites(turretName));
		}
	}
	
	public static void registerSoldier(String name) {
		final String n0 = name + "_p0";
		addToStore(n0, atlas.createSprites(n0));
	}
	
	public static void registerMisc(String name) {
		addToStore(name, atlas.createSprites(name));
	}
	
	private static void addToStore(String name, Array<Sprite> sprites){
		correctSize(sprites);
		spriteStore.add(counter, sprites);
		ntim.put(name, counter);
		itnm.add(counter, name);
		counter ++;
	}
	
	private static void correctSize(Array<Sprite> sprites) {
		for (int i = 0; i < sprites.size; i++) {
			sprites.get(i).setSize(Metrics.sssm, Metrics.sssm);
		}
	}
	
	public static int getIdFromName(String name) {
		
		return ntim.get(name);
	}
	
	public static Array<Sprite> getSharedSpritesFromId(int id) {
		return spriteStore.get(id);
	}
	
	public static Sprite getSharedSpriteFromId(int id, int frame) {
		return spriteStore.get(id).get(frame);
	}
	
	public static Sprite getSharedSpriteFromId(int id) {
		return spriteStore.get(id).first();
	}
	
	public static void dispose() {
		if(atlas != null)
			atlas.dispose();
		atlas = null;
		
		ntim.clear();
		itnm.clear();
		spriteStore.clear();
		counter = 0;
	}
}
