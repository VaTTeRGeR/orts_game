package de.vatterger.game.systems.graphics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;

import de.vatterger.engine.handler.terrain.TerrainHandle;
import de.vatterger.engine.handler.terrain.TerrainTile;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.MeshPool;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.systems.gameplay.TimeSystem;

public class TerrainRenderSystemPrototype extends BaseSystem {

	@Wire(name="camera")
	private OrthographicCamera camera;
	
	private TerrainHandle terrainHandle;
	
	private Array<Texture> textures;
	
	private HashMap<Integer,Mesh> meshes[];
	
	private IntArray activeTiles;
	private IntArray lingeringTiles;

	private MeshPool meshPool = new MeshPool();
	
	private ShaderProgram shader;

	private VertexAttributes vertexAttributes;
	
	private final Vector3 v0 = new Vector3();
	private final Vector3 v1 = new Vector3();
	
	private Profiler profiler = new Profiler("TerrainRenderSystemPrototype", TimeUnit.MICROSECONDS);
	
	@Override
	@SuppressWarnings("unchecked")
	protected void initialize() {
		
		GraphicalProfilerSystem.registerProfiler("TerrainRenderPrototype", Color.RED, profiler);
		
		terrainHandle = new TerrainHandle("assets/terrain/test", 0, 0);
		
		final String[] textureNames = terrainHandle.getTextures();
		
		textures = new Array<>(true, textureNames.length);
		
		//System.out.println("abs: " + Gdx.files.internal("assets/texture/sand1.png").file().getAbsolutePath());
		//System.out.println("exists: " + Gdx.files.internal("assets/texture/sand1.png").file().exists());
		
		meshes = new HashMap[textureNames.length];
		
		activeTiles = new IntArray(true, 256);
		lingeringTiles = new IntArray(true, 256);
		
		for (int texLayer = 0; texLayer < textureNames.length; texLayer++) {
			
			final Texture tex = new Texture(Gdx.files.internal(textureNames[texLayer]), true);
			tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Nearest);
			tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 2f);
			
			meshes[texLayer] = new HashMap<Integer, Mesh>(64);
			
			textures.add(tex);
		}
		
		
		shader = new ShaderProgram(Gdx.files.internal("assets/shader/terrain_simple.vert"),Gdx.files.internal("assets/shader/terrain_single.frag"));
		
		if(!shader.isCompiled()) {
			throw new GdxRuntimeException(shader.getLog());
		}
		
		vertexAttributes = new VertexAttributes(VertexAttribute.Position(), new VertexAttribute(Usage.Generic, 1, "a_alpha"), VertexAttribute.TexCoords(0));
	}
	
	private float[][] unpack(byte[] packed, int cellsX){
		
		final int cellsY = packed.length / cellsX;
		
		final float[][] unpacked = new float[cellsY][cellsX];
		
		for (int i=0,y=0; y < cellsY; y++) {
			for (int x = 0; x < cellsX; x++) {
				unpacked[y][x] = (packed[i++] & 0xFF)/255f;
			}
		}
		
		return unpacked;
	}
	
	private Mesh buildTerrain(float[][] material, float sizeX, float sizeY, Vector3 pos) {
		
		Profiler pA = new Profiler("Terrain mesh: TOTAL", TimeUnit.MICROSECONDS);
		//Profiler pB = new Profiler("Terrain mesh: BUILD", TimeUnit.MICROSECONDS);
		
		//### BEGINNING OF INTERPOLATION ###//
		
		//Profiler pC = new Profiler("Terrain mesh: INTERPOLATE", TimeUnit.MICROSECONDS);
		
		final int MULT = 1;
		final float MULTF = (float) MULT;
		
		//The original data
		float v[][] = material;
		
		//The interpolated data
		float u[][] = new float[(material.length-1) * MULT + 1][(material[0].length-1) * MULT + 1];
		
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
		
		material = u;
		
		//pC.log();
		
		//### END OF INTERPOLATION ###//
		
		final int x_length = material[0].length;
		final int y_length = material.length;
		
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
				vertices[k++] = material[y_length-i-1][j];
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
		
		//Profiler pD = new Profiler("Terrain mesh: UPLOAD", TimeUnit.MICROSECONDS);
		
		//final Mesh mesh = new Mesh(true, true, x_length * y_length, 6 * (x_length - 1) * (y_length - 1), vertexAttributes);
		final Mesh mesh = meshPool.getMesh(vertices.length, indices.length, vertexAttributes);
		
		//System.out.println("Vertices: " + vertices.length);
		//System.out.println("Indices: " + indices.length);
		//System.out.println();
		
		mesh.setVertices(vertices, 0,vertices.length);
		mesh.setIndices(indices, 0, indices.length);

		//pD.log();
		
		pA.log();
		
		
		return mesh;
	}

	private Mesh createDefaultMesh() {
		
		//System.out.println("Creating tile " + tileIndex + ", layer " + tileLayer);
		
		final int cellsX = terrainHandle.getNumCellsX();
		final int cellsY = terrainHandle.getNumCellsY();
		
		final byte[] packedLayerData = new byte[cellsX * cellsY];
		
		Arrays.fill(packedLayerData, (byte)255);
		
		final float[][] unpackedlayerData = unpack(packedLayerData, cellsX);
		
		final float sx = terrainHandle.getCellSizeX();
		final float sy = terrainHandle.getCellSizeY();
		
		final Mesh mesh = buildTerrain(unpackedlayerData, sx, sy, new Vector3());
		
		return mesh;
	}
	
	private Mesh createMesh(int tileIndex, int tileLayer) {
		
		//System.out.println("Creating tile " + tileIndex + ", layer " + tileLayer);
		
		final TerrainTile tile = terrainHandle.getTile(tileIndex);
		
		final byte[] packedLayerData = tile.getLayer(tileLayer);
		final float[][] unpackedlayerData = unpack(packedLayerData, tile.getCellsX());
		
		final float sx = tile.getCellSizeX();
		final float sy = tile.getCellSizeY();
		
		final Vector3 pos = new Vector3(tile.getBorderX1(), tile.getBorderY1(), 0f);
		
		final Mesh mesh = buildTerrain(unpackedlayerData, sx, sy, pos);
		
		meshes[tileLayer].put(tileIndex, mesh);
		
		return mesh;
	}
	
	private void removeMeshes(int tileIndex) {
		
		System.out.println("Decommisioning tile " + tileIndex);
		
		for (int i = 0; i < meshes.length; i++) {
			
			final Mesh mesh = meshes[i].remove(tileIndex);

			if(mesh != null) {
				meshPool.free(mesh);
			}
		}
	}
	
	@Override
	protected void begin() {
		
		profiler.start();
		
		terrainHandle.finishLoading();
		
		createLoadedMeshes();
		
		meshPool.trim();
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		shader.begin();
		
		shader.setUniformMatrix("u_projTrans", camera.combined);

		if(shader.hasUniform("time")) {
			shader.setUniformf("time", (float)TimeSystem.getCurrentTimeSeconds());
		}

		shader.setUniformi("u_tex0", 0);
	}

	@Override
	protected void processSystem () {

		Math2D.castRay(v0.set(0f, Gdx.graphics.getHeight(), 0f), camera);
		Math2D.castRay(v1.set(Gdx.graphics.getWidth(), 0f, 0f), camera);
		
		final float offsetX = terrainHandle.getTileSizeX() * 3;
		final float offsetY = terrainHandle.getTileSizeY() * 3;
		
		final float x1 = v0.x - offsetX;
		final float y1 = v0.y - offsetY;
		final float x2 = v1.x + offsetX;
		final float y2 = v1.y + offsetY;
		
		lingeringTiles.clear();
		
		lingeringTiles.addAll(activeTiles);
		
		activeTiles.clear();
		
		terrainHandle.getTiles(x1, y1, x2, y2, activeTiles);
		
		for (int i = 0; i < lingeringTiles.size; i++) {
			
			final int tileIndex = lingeringTiles.get(i);
			
			if(!activeTiles.contains(tileIndex)) {
				removeMeshes(tileIndex);
				terrainHandle.unload(tileIndex, false);
			}
		}
		
		for (int textureIndex = 0; textureIndex < textures.size; textureIndex++) {
			renderLayer(textureIndex);
		}
	}
	
	private void renderLayer(int tileLayer) {
		
		textures.get(tileLayer).bind();
		
		for (int i = 0; i < activeTiles.size; i++) {
			renderTile(activeTiles.get(i), tileLayer);
		}
	}
	
	private void renderTile(int tileIndex, int tileLayer) {
		
		final TerrainTile tile = terrainHandle.getTile(tileIndex);
		
		if(tile == null) {
			return;
		}
		
		final float x1 = Math2D.round(tile.getBorderX1(), Metrics.ppm);
		final float y1 = Math2D.round(tile.getBorderY1(), Metrics.ppm);
		
		shader.setUniform2fv("u_offset", new float[] {x1, y1}, 0, 2);

		Mesh mesh = meshes[tileLayer].get(tileIndex);
		
		if(mesh == null) {
			return;
		}

		mesh.render(shader, GL20.GL_TRIANGLES);
	}
	
	private void createLoadedMeshes() {
		
		final long tStart = System.nanoTime();
		
		for (int i = 0; i < activeTiles.size; i++) {
		
			final int tileIndex = activeTiles.items[i];
			final TerrainTile tile = terrainHandle.getTile(tileIndex);
			
			if(tile == null) {
				continue;
			}
			
			final byte[] textures = tile.getTextures();
				
			for (int j = 0; j < textures.length; j++) {
				
				if((System.nanoTime() - tStart) > 2000000) {
					break;
				}
				
				if(meshes[j].get(tileIndex) == null) {
					
					//Profiler p = new Profiler("creating mesh " + tileIndex, TimeUnit.MICROSECONDS);
					
					createMesh(tileIndex, textures[j]);
					
					//p.log();
				}
			}
			
		}

	}
	
	@Override
	protected void end() {
		
		shader.end();
		
		profiler.stop();
	}
	
	@Override
	protected void dispose() {
		
		for (int i = 0; i < meshes.length; i++) {
			
			for(Mesh mesh : meshes[i].values()) {
				mesh.dispose();
			}
			
			meshes[i].clear();
		}

		
		shader.dispose();
		
		for (Texture tex : textures.items) {
			tex.dispose();
		}
	}
}
