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
	private static final ArrayList<Array<Sprite>> sprites;
	private static int counter;

	static {
		ntim = new HashMap<String, Integer>();
		itnm = new ArrayList<String>();
		sprites = new ArrayList<Array<Sprite>>();
		counter = 0;
	}
	
	public static void initialize(float sssm) {
		dispose();
		atlas = new TextureAtlas("atlas/packfile.atlas");
	}
	
	public static void registerTank(String name, int turrets) {
		Array<Sprite> hullSprites = atlas.createSprites(name+"_h");
		for (int i = 0; i < hullSprites.size; i++) {hullSprites.get(i).setSize(Metrics.sssm, Metrics.sssm);}
		sprites.add(counter, hullSprites);
		ntim.put(name+"_h", counter);
		itnm.add(counter, name+"_h");
		counter ++;
		for (int i = 0; i < turrets; i++) {
			hullSprites = atlas.createSprites(name+"_t"+i);
			for (int j = 0; j < hullSprites.size; j++) {hullSprites.get(j).setSize(Metrics.sssm, Metrics.sssm);}
			sprites.add(counter, hullSprites);
			ntim.put(name+"_t"+i, counter);
			itnm.add(counter, name+"_t"+i);
			counter ++;
		}
	}
	
	public static void registerMisc(String name) {
		Array<Sprite> miscSprites = atlas.createSprites(name);
		for (int i = 0; i < miscSprites.size; i++) {miscSprites.get(i).setSize(Metrics.sssm, Metrics.sssm);}
		sprites.add(counter, miscSprites);
		ntim.put(name, counter);
		itnm.add(counter, name);
		counter ++;
	}
	
	public static int getIdFromName(String name) {
		
		return ntim.get(name);
	}
	
	public static Array<Sprite> getSharedSpritesFromId(int id) {
		return sprites.get(id);
	}
	
	public static Sprite getSharedSpriteFromId(int id, int frame) {
		return sprites.get(id).get(frame);
	}
	
	public static Sprite getSharedSpriteFromId(int id) {
		return sprites.get(id).first();
	}
	
	public static void dispose() {
		if(atlas != null)
			atlas.dispose();
		atlas = null;
		
		ntim.clear();
		itnm.clear();
		sprites.clear();
		counter = 0;
	}
}
