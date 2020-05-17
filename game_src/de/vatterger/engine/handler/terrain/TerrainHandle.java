package de.vatterger.engine.handler.terrain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import de.vatterger.engine.util.AtomicRingBuffer;
import de.vatterger.engine.util.JSONPropertiesHandler;

public class TerrainHandle implements Disposable {
	
	private final ExecutorService tileLoadExecutor;
	private final AtomicRingBuffer<TerrainTile> tileLoadedQueue;
	
	private final ExecutorService tileMeshExecutor;
	private final AtomicRingBuffer<TerrainTileMeshData> tileMeshDataQueue;
	
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
	
	private final int numTextures;
	private final String[] textures;
	
	private final TerrainTile[] tiles;
	private final boolean[] tileIsLoading;
	private final boolean[] tileIsRequested;

	private final TerrainTileMeshData[][] tileMeshDatas;
	private final boolean[][] tileMeshIsLoading;
	
	public TerrainHandle(String mapFolder, float offsetX, float offsetY) {
		
		tileLoadExecutor = Executors.newSingleThreadExecutor();
		tileLoadedQueue = new AtomicRingBuffer<>(512);
		
		tileMeshExecutor = Executors.newSingleThreadExecutor();
		tileMeshDataQueue = new AtomicRingBuffer<>(512);
		
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
		
		numTextures = texturesJson.size;
		textures = new String[numTextures];
		
		for (int i = 0; i < textures.length; i++) {
			textures[i] = texturesJson.getString(i);
		}
		
		tiles = new TerrainTile[numTilesX * numTilesY];
		tileIsLoading = new boolean[numTilesX * numTilesY];
		tileIsRequested = new boolean[numTilesX * numTilesY];

		Arrays.fill(tileIsLoading, false);

		tileMeshDatas = new TerrainTileMeshData[numTilesX * numTilesY][numTextures];
		tileMeshIsLoading = new boolean[numTilesX * numTilesY][numTextures];
		
		for (boolean[] layer : tileMeshIsLoading) {
			Arrays.fill(layer, false);
		}
	}
	
	public String[] getTextures() {
		return textures;
	}
	
	public void getTileIndices(float x1, float y1, float x2, float y2, IntArray result) {

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
	
	public void reloadTileMeshData(int tileIndex, int tileLayer) {
		createTerrainTileMeshData(tileIndex, tileLayer);
	}
	
	public TerrainTileMeshData getTileMeshData(int tileIndex, int tileLayer) {
		
		if(tileMeshDatas[tileIndex][tileLayer] == null && !tileMeshIsLoading[tileIndex][tileLayer]) {
			createTerrainTileMeshData(tileIndex, tileLayer);
		}

		return tileMeshDatas[tileIndex][tileLayer];
	}
	
	public void finishLoading() {
		
		while(tileLoadedQueue.has()) {
			
			final TerrainTile tile = tileLoadedQueue.get();
			final int tileIndex = tile.getTileIndex();
			
			tileIsLoading[tileIndex] = false;
			
			if(!tileIsRequested[tileIndex]) {
				continue;
			}
			
			tiles[tileIndex] = tile;
			
			byte[] tileTextures = tile.getTextures();
			
			for (int i = 0; i < tileTextures.length; i++) {
				createTerrainTileMeshData(tileIndex, tileTextures[i]);
			}
			
			//System.out.println("Finished loading tile " + tileIndex);
		}

		while(tileMeshDataQueue.has()) {
			
			final TerrainTileMeshData tileMeshData = tileMeshDataQueue.get();
			
			tileMeshIsLoading[tileMeshData.tileIndex][tileMeshData.tileLayer] = false;
			
			if(!tileIsRequested[tileMeshData.tileIndex]) {
				continue;
			}
			
			tileMeshDatas[tileMeshData.tileIndex][tileMeshData.tileLayer] = tileMeshData;
		}
	}
	
	public void load(int tileIndex) {
		load(tileIndex, false);
	}
	
	public void load(int tileIndex, boolean fromDisk) {
		
		tileIsRequested[tileIndex] = true;
		
		if(tileIsLoading[tileIndex]) {
			return;
		}
		
		if(tiles[tileIndex] == null || fromDisk) {
			
			tileIsLoading[tileIndex] = true;
			
			tileLoadExecutor.execute(() -> {
				
				//Profiler p = new Profiler("Loading tile", TimeUnit.MICROSECONDS);
				
				final TerrainTile tile = new TerrainTile(tileIndex, this);
				
				//System.out.println("Loaded tile " + tileIndex + " from disk.");
				
				while(!tileLoadedQueue.canWrite()) {
					Thread.yield();
				}

				tileLoadedQueue.put(tile) ;
				
				//p.log();
			});
		}
	}
	
	public void unload(int tileIndex) {
		unload(tileIndex, true);
	}
	
	public void unload(int tileIndex, boolean autoSave) {

		final TerrainTile tile = tiles[tileIndex];
		
		tileIsRequested[tileIndex] = false;
		
		if(tile == null) {
			return;
		}
		
		tiles[tileIndex] = null;
		
		final TerrainTileMeshData[] tileMeshData = tileMeshDatas[tileIndex];
		
		Arrays.fill(tileMeshData, null);
		
		if(autoSave && tile.isModified()) {
			save(tile);
		}
	}
	
	public void save(int tileIndex) {
		
		final TerrainTile tile = tiles[tileIndex];
		
		if(tile == null) {
			return;
		}
		
		save(tile);
	}
	
	private void save(TerrainTile tile) {
		
		tile.rebuildData();
		
		tileLoadExecutor.execute(() -> {
			//Profiler p = new Profiler("Writing tile", TimeUnit.MICROSECONDS);
			tile.writeToDisk();
			//p.log();
		});
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
		tileLoadExecutor.shutdown();
		tileMeshExecutor.shutdown();
	}

	private void createTerrainTileMeshData(int tileIndex, int tileLayer) {

		tileMeshIsLoading[tileIndex][tileLayer] = true;
		
		final TerrainTile tile = getTile(tileIndex);
		
		final float sizeX = tile.getCellSizeX();
		final float sizeY = tile.getCellSizeY();
		
		final int cellsX = tile.getCellsX();
		final int cellsY = tile.getCellsY();
		
		final Vector3 pos = new Vector3(tile.getBorderX1(), tile.getBorderY1(), 0f);
		
		final byte[] packed = tile.getLayer(tileLayer).clone();
		
		tileMeshExecutor.execute(() -> {
			
			final TerrainTileMeshData tileMeshData = buildTerrainMeshData(tileIndex, tileLayer, packed, sizeX, sizeY, cellsX, cellsY, pos);
			
			while(!tileMeshDataQueue.canWrite()) {
				Thread.yield();
			}
			
			tileMeshDataQueue.put(tileMeshData);
		});
	}
	
	private TerrainTileMeshData buildTerrainMeshData(int tileIndex, int tileLayer, byte[] packed, float sizeX, float sizeY, int cellsX, int cellsY, Vector3 pos) {
		
		
		final float[][] unpacked = new float[cellsY][cellsX];
		
		for (int i=0,y=0; y < cellsY; y++) {
			for (int x = 0; x < cellsX; x++) {
				unpacked[y][x] = (packed[i++] & 0xFF) / 255f;
			}
		}
		
		//Profiler pA = new Profiler("Terrain mesh: TOTAL", TimeUnit.MICROSECONDS);
		//Profiler pB = new Profiler("Terrain mesh: BUILD", TimeUnit.MICROSECONDS);
		
		//### BEGINNING OF INTERPOLATION ###//
		
		//Profiler pC = new Profiler("Terrain mesh: INTERPOLATE", TimeUnit.MICROSECONDS);
		
		final int MULT = 1;
		final float MULTF = (float) MULT;
		
		//The original data
		float v[][] = unpacked;
		
		if(MULT > 1) {
		
			//The interpolated data
			float u[][] = new float[(v.length-1) * MULT + 1][(v[0].length-1) * MULT + 1];
			
			for (int i = 0; i < u.length; i++) {
				for (int j = 0; j < u[0].length; j++) {
					
					int iV = i/MULT;
					int jV = j/MULT;
					
					if(i==u.length-1) {
						iV--;
					}
					
					if(j==u[0].length-1) {
						jV--;
					}
					
					int iR = iV*MULT;
					int jR = jV*MULT;
					
					float di = i-iR;
					float dj = j-jR;
					
					float vLL = v[ iV		][ jV	];
					float vLR = v[ iV+1	][ jV	];
					float vUL = v[ iV		][ jV+1	];
					float vUR = v[ iV+1	][ jV+1	];
					
					// Bi-linear interpolation
					u[i][j] = (vUL*dj/MULTF+vLL*(1f-dj/MULTF))*(1f-di/MULTF) + (vUR*dj/MULTF+vLR*(1f-dj/MULTF))*(di/MULTF);
				}
			}
			
			v = u;
		}
		
		//pC.log();
		
		//### END OF INTERPOLATION ###//
		
		final int x_length = v[0].length;
		final int y_length = v.length;
		
		final VertexAttributes vertexAttributes = new VertexAttributes(VertexAttribute.Position(), new VertexAttribute(Usage.Generic, 1, "a_alpha"), VertexAttribute.TexCoords(0));
		
		final float[] vertices	= new float[x_length * y_length*(vertexAttributes.vertexSize / 4)];
		final short[] indices	= new short[6 * (x_length - 1) * (y_length - 1)];
		
		final float texture_scale = 40f; //40f
		
		final float x_space = sizeX / MULTF;
		final float y_space = sizeY / MULTF; // * MathUtils.sin(MathUtils.PI * 0.25f);
		
		int k = 0;
		for (int i = 0; i < y_length; i++) {
			for (int j = 0; j < x_length; j++) {
				vertices[k++] = j * x_space;
				vertices[k++] = i * y_space;
				vertices[k++] = 0f;//10*material[y_length-i-1][j]; // HEIGHT HEREE!
				vertices[k++] = v[y_length-i-1][j];
				vertices[k++] = ( i * y_space + pos.y ) * texture_scale;
				vertices[k++] = ( j * x_space + pos.x ) * texture_scale;
			}
		}
		
		// O0	-	C0
		
		
		
		// C1	-	O1
		
		k = 0;
		for (int i = 0; i < y_length-1; i++) {
			for (int j = 0; j < x_length-1; j++) {
				// Alternating pattern
				if( (i + j) % 2 == 0) {
					indices[k++] = (short)(i * x_length + j);// O0
					indices[k++] = (short)(i * x_length + j + 1);// C0
					indices[k++] = (short)(i * x_length + j + x_length);// C1
					indices[k++] = (short)(i * x_length + j + 1);// C0
					indices[k++] = (short)(i * x_length + j + 1 + x_length); // O1
					indices[k++] = (short)(i * x_length + j + x_length); // C1
					
				} else {
					indices[k++] = (short)(i * x_length + j);// O0
					indices[k++] = (short)(i * x_length + j + 1 + x_length); // O1
					indices[k++] = (short)(i * x_length + j + x_length);// C1
					indices[k++] = (short)(i * x_length + j);// O0
					indices[k++] = (short)(i * x_length + j + 1);// C0
					indices[k++] = (short)(i * x_length + j + 1 + x_length); // O1
				}
			}
		}
		
		//pB.log();
		
		//System.out.println("Vertices: " + vertices.length);
		//System.out.println("Indices: " + indices.length);
		//System.out.println();
		
		//pA.log();
		
		
		return new TerrainTileMeshData(tileIndex, tileLayer, vertices, indices);
	}
}
