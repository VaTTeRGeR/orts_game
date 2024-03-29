
package de.vatterger.game.systems.graphics;

import java.util.concurrent.TimeUnit;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;

import de.vatterger.engine.handler.terrain.TerrainHandle;
import de.vatterger.engine.handler.terrain.TerrainTile;
import de.vatterger.engine.handler.terrain.TerrainTileMeshData;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.MeshPool;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.systems.gameplay.TimeSystem;

public class TerrainRenderSystemPrototype extends BaseSystem {

	/** Time limit for building and uploading terrain to GPU. Needed to not block the main thread. */
	private static final int TERRAIN_BUILD_NANOS_MAX = 4_000_000;

	@Wire(name = "camera") private OrthographicCamera camera;

	/** A Handle Class for loading and saving terrain tiles */
	private TerrainHandle terrainHandle;

	/** Image Textures for each layer */
	private Array<Texture> textures;

	/** One integer-indexed map of meshes per tileLayer. Get Mesh with: meshes[tileLayer].get(tileIndex) */
	private IntMap<Mesh>[] meshes;

	/** Contains the tiles that overlap the view area */
	private final IntArray visibleTiles = new IntArray(true, 256);
	/** Contains the tiles that are near the view area */
	private final IntArray loadedTiles = new IntArray(true, 512);
	/** Contains the tiles that were near the view area in the previous frame */
	private final IntArray lingeringTiles = new IntArray(true, 512);

	/** VertexAttributes for rendering the Terrain Mesh */
	private final VertexAttributes vertexAttributes = new VertexAttributes(VertexAttribute.Position(),
		new VertexAttribute(Usage.Generic, 1, "a_alpha"), VertexAttribute.TexCoords(0));

	/** Shader for rendering the Terrain Mesh */
	private ShaderProgram shader;

	/** Allows to reusing Meshes */
	private final MeshPool meshPool = new MeshPool(-1);

	/** Profiler for frametime waterfall diagram */
	private final Profiler profiler = new Profiler("TerrainRenderSystemPrototype", TimeUnit.MICROSECONDS);

	@Override
	@SuppressWarnings("unchecked")
	protected void initialize () {

		GraphicalProfilerSystem.registerProfiler("TerrainRenderPrototype", Color.RED, profiler);

		terrainHandle = new TerrainHandle("assets/terrain/map01", 0, 0);

		final String[] texturePathStrings = terrainHandle.getTextures();
		final int numTextures = terrainHandle.getTextures().length;

		textures = new Array<>(true, numTextures);

		// System.out.println("abs: " + Gdx.files.internal("assets/texture/sand1.png").file().getAbsolutePath());
		// System.out.println("exists: " + Gdx.files.internal("assets/texture/sand1.png").file().exists());

		meshes = new IntMap[numTextures];

		for (int texLayer = 0; texLayer < numTextures; texLayer++) {

			final Texture tex = new Texture(Gdx.files.internal(texturePathStrings[texLayer]), true);

			tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Nearest);
			tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

			Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 2f);

			meshes[texLayer] = new IntMap<Mesh>(128);

			textures.add(tex);
		}

		shader = new ShaderProgram(Gdx.files.internal("assets/shader/terrain_simple.vert"),
			Gdx.files.internal("assets/shader/terrain_single.frag"));

		if (!shader.isCompiled()) {
			throw new GdxRuntimeException(shader.getLog());
		}
	}

	private Mesh buildTerrain (int tileIndex, int tileLayer) {

		TerrainTileMeshData meshData = terrainHandle.getTileMeshData(tileIndex, tileLayer);

		if (meshData == null) {
			return null;
		}

		final float[] vertices = meshData.vertices;
		final short[] indices = meshData.indices;

		final Mesh mesh = meshPool.getMesh(vertices.length, indices.length, vertexAttributes, 100);

		mesh.setVertices(vertices, 0, vertices.length);
		mesh.setIndices(indices, 0, indices.length);

		return mesh;
	}

	private Mesh createTileMesh (int tileIndex, int tileLayer) {

		final Mesh mesh = buildTerrain(tileIndex, tileLayer);

		if (mesh == null) {
			return null;
		}

		removeTileMesh(tileIndex, tileLayer);

		meshes[tileLayer].put(tileIndex, mesh);

		return mesh;
	}

	private void removeTileMeshes (int tileIndex) {

		for (int i = 0; i < meshes.length; i++) {
			removeTileMesh(tileIndex, i);
		}
	}

	private void removeTileMesh (int tileIndex, int tileLayer) {

		final Mesh mesh = meshes[tileLayer].remove(tileIndex);

		if (mesh != null) {
			meshPool.free(mesh);
		}
	}

	@Override
	protected void begin () {

		// Profiler p_setup = new Profiler("SETUP", TimeUnit.MICROSECONDS);

		// int meshCounter = 0;

		// for (IntMap<Mesh> map : meshes) {
		// meshCounter += map.size;
		// }

		// System.out.println("Number of active/loaded/lingering meshes: " + visibleTiles.size + "/" + loadedTiles.size + "/" +
		// lingeringTiles.size);

		profiler.start();

		terrainHandle.finishLoading();

		createLoadedMeshes();

		meshPool.trim();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		shader.bind();

		shader.setUniformMatrix("u_projTrans", camera.combined);

		if (shader.hasUniform("time")) {
			shader.setUniformf("time", (float)TimeSystem.getCurrentTimeSeconds());
		}

		shader.setUniformi("u_tex0", 0);

		// p_setup.log();
	}

	@Override
	protected void processSystem () {

		// Profiler p_cull = new Profiler("CULL", TimeUnit.MICROSECONDS);

		final Vector3 v0 = new Vector3();
		final Vector3 v1 = new Vector3();

		Math2D.castRay(v0.set(0f, Gdx.graphics.getHeight(), 0f), camera);
		Math2D.castRay(v1.set(Gdx.graphics.getWidth(), 0f, 0f), camera);

		// Border
		final float offsetX = terrainHandle.getTileSizeX() * 2;
		final float offsetY = terrainHandle.getTileSizeY() * 2;

		final float x1 = v0.x;
		final float y1 = v0.y;
		final float x2 = v1.x;
		final float y2 = v1.y;

		lingeringTiles.clear();
		lingeringTiles.addAll(loadedTiles);

		visibleTiles.clear();
		terrainHandle.getTileIndices(x1, y1, x2, y2, visibleTiles);

		loadedTiles.clear();
		terrainHandle.getTileIndices(x1 - offsetX, y1 - offsetY, x2 + offsetX, y2 + offsetY, loadedTiles);

		// System.out.println("Visible: " + visibleTiles.size + ", Loaded: " + loadedTiles.size);

		// Returned Tiles are sorted already because of the iteration order.
		// This algorithm depends on the tileIndices being sorted.
		for (int i = 0, j = 0; i < lingeringTiles.size; i++) {

			final int lingeringTile = lingeringTiles.get(i);

			boolean found = false;

			while (j < loadedTiles.size && lingeringTile >= loadedTiles.get(j)) {

				if (lingeringTile == loadedTiles.get(j++)) {
					found = true;
					break;
				}
			}

			if (!found) {
				removeTileMeshes(lingeringTile);
				terrainHandle.unload(lingeringTile, /* writing to disk: */true);
			}
		}

		
		// Change a few layer values to test re-generation
		if (Gdx.input.isKeyPressed(Keys.H)) {
			Math2D.castMouseRay(v0, camera);
			TerrainTile tile = terrainHandle.getTile(v0.x, v0.y);
			if (tile != null) {

				byte[] layer = tile.getLayer(1);

				for (int i = 0; i < MathUtils.random(100); i++) {
					layer[MathUtils.random(layer.length - 1)] = (byte)MathUtils.random(255);
				}

				tile.setModified();
				terrainHandle.save(tile.getTileIndex());

				terrainHandle.reloadTileMeshData(tile.getTileIndex(), 1);
			}
		}

		// p_cull.log();

		// System.out.println(Arrays.toString(Arrays.copyOf(activeTiles.items, activeTiles.size)));

		// Profiler p_render = new Profiler("RENDER", TimeUnit.MICROSECONDS);

		for (int textureIndex = 0; textureIndex < textures.size; textureIndex++) {
			renderLayer(textureIndex);
		}

		// p_render.log();
	}

	private void renderLayer (int tileLayer) {

		textures.get(tileLayer).bind();

		for (int i = 0; i < visibleTiles.size; i++) {
			renderTile(visibleTiles.get(i), tileLayer);
		}
	}

	private void renderTile (int tileIndex, int tileLayer) {

		final TerrainTile tile = terrainHandle.getTile(tileIndex);

		if (tile == null) {
			return;
		}

		final Mesh mesh = meshes[tileLayer].get(tileIndex);

		if (mesh == null) {
			return;
		}

		final float x1 = Math2D.round(tile.getBorderX1(), Metrics.ppm);
		final float y1 = Math2D.round(tile.getBorderY1(), Metrics.ppm);

		shader.setUniform2fv("u_offset", new float[] {x1, y1}, 0, 2);

		mesh.render(shader, GL20.GL_TRIANGLES);
	}

	private void createLoadedMeshes () {

		final long tStart = System.nanoTime();

		for (int i = 0; i < loadedTiles.size; i++) {

			final int tileIndex = loadedTiles.items[i];
			final TerrainTile tile = terrainHandle.getTile(tileIndex);

			if (tile == null) {
				continue;
			}

			for (int tileLayer = 0; tileLayer < terrainHandle.numTextures(); tileLayer++) {
				if (tile.isLayerEnabled(tileLayer) && terrainHandle.isTileMeshDataUpdated(tileIndex, tileLayer)) {
					createTileMesh(tileIndex, tileLayer);
				}
			}

			if ((System.nanoTime() - tStart) > TERRAIN_BUILD_NANOS_MAX) {
				break;
			}
		}

	}

	@Override
	protected void end () {
		profiler.stop();
	}

	@Override
	protected void dispose () {

		for (int i = 0; i < meshes.length; i++) {

			for (Mesh mesh : meshes[i].values()) {
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
