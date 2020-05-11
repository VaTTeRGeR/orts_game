package de.vatterger.engine.handler.terrain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import de.vatterger.engine.util.AtomicRingBuffer;
import de.vatterger.engine.util.JSONPropertiesHandler;
import de.vatterger.engine.util.Profiler;

public class TerrainHandle implements Disposable {
	
	private final ExecutorService executor;
	private final AtomicRingBuffer<TerrainTile> loadedQueue;
	
	private final JSONPropertiesHandler mapIndexHandler;
	
	protected final String mapFolderString;
	
	protected final int numTilesX, numTilesY;
	protected final int numCellsX, numCellsY;
	protected final float cellSizeX, cellSizeY;

	protected final float tileSizeX, tileSizeY;
	protected final float tileSizeXInv, tileSizeYInv;
	
	protected final float mapSizeX, mapSizeY;
	
	protected final float mapBorderX1, mapBorderY1;
	protected final float mapBorderX2, mapBorderY2;
	
	private final String[] textures;
	
	private final TerrainTile[] tiles;
	private final boolean[] tileIsLoading;
	
	public TerrainHandle(String mapFolder, float offsetX, float offsetY) {
		
		executor = Executors.newSingleThreadExecutor();
		
		loadedQueue = new AtomicRingBuffer<>(128);
		
		this.mapFolderString = mapFolder;
		
		final Path folderPath = Paths.get(mapFolder);
		
		if(!Files.exists(folderPath)) {
			try {
				Files.createDirectory(folderPath);
			} catch (IOException e) {
				throw new IllegalStateException("Could not create map folder: " + folderPath);
			}
		}
		
		final Path indexFilePath = Paths.get(mapFolder, "index.json");
		
		mapIndexHandler = new JSONPropertiesHandler(indexFilePath.toString());
		final JsonValue mapIndexJson = mapIndexHandler.getJsonValue();
		
		if(!mapIndexHandler.exists()) {
			
			
			mapIndexJson.addChild("numTilesX", new JsonValue(-1));
			mapIndexJson.addChild("numTilesY", new JsonValue(-1));
			mapIndexJson.addChild("numCellsX", new JsonValue(-1));
			mapIndexJson.addChild("numCellsY", new JsonValue(-1));
			mapIndexJson.addChild("cellSizeX", new JsonValue(-1));
			mapIndexJson.addChild("cellSizeY", new JsonValue(-1));

			final JsonValue textures = new JsonValue(ValueType.array);
			
			//textures.addChild(new JsonValue("assets/texture/grass2.png"));
			
			mapIndexJson.addChild("textures", textures);
			
			mapIndexHandler.save();
		}
		
		numTilesX = mapIndexJson.getInt("numTilesX");
		numTilesY = mapIndexJson.getInt("numTilesY");
		
		numCellsX = mapIndexJson.getInt("numCellsX");
		numCellsY = mapIndexJson.getInt("numCellsY");

		cellSizeX = mapIndexJson.getFloat("cellSizeX");
		cellSizeY = mapIndexJson.getFloat("cellSizeY");
		
		tileSizeX = cellSizeX * (numCellsX - 1);
		tileSizeY = cellSizeY * (numCellsY - 1);
		
		tileSizeXInv = 1f/tileSizeX;
		tileSizeYInv = 1f/tileSizeY;
		
		mapSizeX = numTilesX * tileSizeX;
		mapSizeY = numTilesY * tileSizeY;
		
		mapBorderX1 = offsetX;
		mapBorderY1 = offsetY;
		
		mapBorderX2 = mapBorderX1 + mapSizeX;
		mapBorderY2 = mapBorderY1 + mapSizeY;
		
		final JsonValue texturesJson = mapIndexJson.get("textures");
		
		textures = new String[texturesJson.size];
		
		for (int i = 0; i < textures.length; i++) {
			textures[i] = texturesJson.getString(i);
		}
		
		tiles = new TerrainTile[numTilesX * numTilesY];
		
		tileIsLoading = new boolean[numTilesX * numTilesY];
		
		Arrays.fill(tileIsLoading, false);
	}
	
	public String[] getTextures() {
		return textures;
	}
	
	public void getTiles(float x1, float y1, float x2, float y2, IntArray result) {

		if(x1 >= mapBorderX2 || y1 >= mapBorderY2 || x2 < mapBorderX1 || y2 < mapBorderY1) {
			return;
		}
		
		final int ix1 = MathUtils.clamp((int)((x1 - mapBorderX1)/tileSizeX), 0, numTilesX - 1);
		final int iy1 = MathUtils.clamp((int)((y1 - mapBorderY1)/tileSizeY), 0, numTilesY - 1);
		final int ix2 = MathUtils.clamp((int)((x2 - mapBorderX1)/tileSizeX), 0, numTilesX - 1);
		final int iy2 = MathUtils.clamp((int)((y2 - mapBorderY1)/tileSizeY), 0, numTilesY - 1);
		
		for (int y = iy1; y <= iy2; y++) {
			for (int x = ix1; x <= ix2; x++) {
				result.add(x + y * numTilesX);
			}
		}
	}
	
	public TerrainTile getTile(float x,float y) {
		return getTile(tileIndex(x,y));
	}
	
	public TerrainTile getTile(int tileIndex) {
		
		if(tiles[tileIndex] == null && !tileIsLoading[tileIndex]) {
			load(tileIndex);
		}

		return tiles[tileIndex];
	}
	
	public void finishLoading() {
		
		while(loadedQueue.has()) {
			
			final TerrainTile tile = loadedQueue.get();
			final int tileIndex = tile.getTileIndex();
			
			tiles[tileIndex] = tile;
			tileIsLoading[tileIndex] = false;

			//System.out.println("Finished loading tile " + tileIndex);
		}
	}
	
	public void load(int tileIndex) {
		load(tileIndex, false);
	}
	
	public void load(int tileIndex, boolean fromDisk) {
		
		if(tileIsLoading[tileIndex]) {
			return;
		}
		
		if(tiles[tileIndex] == null || fromDisk) {
			
			tileIsLoading[tileIndex] = true;
			
			executor.execute(() -> {
				
				Profiler p = new Profiler("Loading tile", TimeUnit.MICROSECONDS);
				
				final TerrainTile tile = new TerrainTile(tileIndex, this);
				
				//System.out.println("Loaded tile " + tileIndex + " from disk.");
				
				while(!loadedQueue.canWrite()) {
					Thread.yield();
				}

				loadedQueue.put(tile) ;
				
				p.log();
			});
		}
	}
	
	public void unload(int tileIndex) {
		unload(tileIndex, true);
	}
	
	public void unload(int tileIndex, boolean autoSave) {

		final TerrainTile tile = tiles[tileIndex];
		
		if(tile == null) {
			return;
		}
		
		tiles[tileIndex] = null;
		
		if(autoSave && tile.isModified()) {
			
			tile.rebuildData();
			
			executor.execute(() -> {
				Profiler p = new Profiler("Writing tile", TimeUnit.MICROSECONDS);
				tile.writeToDisk();
				p.log();
			});
		}
	}
	
	public int tileIndex (float x, float y) {
		return (int)((x - mapBorderX1) * tileSizeXInv) + (int)((y - mapBorderY1) * tileSizeYInv) * numTilesX;
	}

	public float xFromTileIndex (int tileIndex) {
		return (tileIndex % numTilesX) * tileSizeX + mapBorderX1;
	}

	public float yFromTileIndex (int tileIndex) {
		return (tileIndex / numTilesX) * tileSizeX + mapBorderY1;
	}

	public int getNumTilesX () {
		return numTilesX;
	}

	public int getNumTilesY () {
		return numTilesY;
	}

	public int getNumCellsX () {
		return numCellsX;
	}

	public int getNumCellsY () {
		return numCellsY;
	}

	public float getCellSizeX () {
		return cellSizeX;
	}

	public float getCellSizeY () {
		return cellSizeY;
	}
	
	public float getTileSizeX () {
		return tileSizeX;
	}
	
	public float getTileSizeY () {
		return tileSizeY;
	}

	@Override
	public void dispose () {
		executor.shutdown();
	}
}
