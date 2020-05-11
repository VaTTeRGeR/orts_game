package de.vatterger.game.systems.graphics;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.TerrainHeightField;
import de.vatterger.game.systems.gameplay.TimeSystem;

public class TerrainRenderSystem extends IteratingSystem {

	@Wire(name="camera")
	private OrthographicCamera camera;
	
	private ComponentMapper<AbsolutePosition>	apm;
	private ComponentMapper<TerrainHeightField>	thfm;
	
	private Texture tex0;
	private Texture tex1;
	private Texture tex2;
	private Texture tex3;
	private Texture tex4;
	
	private HashMap<Integer,Mesh> meshes;
	
	private Pool<Mesh> meshPool = new Pool<Mesh>() {
		@Override
		protected Mesh newObject() {
			return new Mesh(false, false, 20000, 20000, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0)));
		}
	};
	
	private ShaderProgram shader;
	
	private float time = 0f;
	
	private Profiler profiler = new Profiler("TerrainRenderSystem", TimeUnit.MICROSECONDS);
	
	public TerrainRenderSystem() {
		super(Aspect.all(AbsolutePosition.class, TerrainHeightField.class).exclude(Culled.class));
	}
	
	@Override
	protected void initialize() {
		
		GraphicalProfilerSystem.registerProfiler("TerrainRender", Color.RED, profiler);
		
		//System.out.println("abs: " + Gdx.files.internal("assets/texture/sand1.png").file().getAbsolutePath());
		//System.out.println("exists: " + Gdx.files.internal("assets/texture/sand1.png").file().exists());
		
		//tex0 = new Texture(Gdx.files.internal("assets/texture/colorgrid.png"), true);
		//tex0 = new Texture(Gdx.files.internal("assets/texture/uvgrid.png"), true);
		tex0 = new Texture(Gdx.files.internal("assets/texture/water1.png"), true);
		tex0.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex0.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 2f);
		
		tex1 = new Texture(Gdx.files.internal("assets/texture/sand1.png"), true);
		tex1.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex1.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 2f);
		
		tex2 = new Texture(Gdx.files.internal("assets/texture/sand2.png"), true);
		tex2.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex2.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 2f);
		
		tex3 = new Texture(Gdx.files.internal("assets/texture/grass2.png"), true);
		tex3.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex3.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 2f);
		
		tex4 = new Texture(Gdx.files.internal("assets/texture/grass4.png"), true);
		tex4.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex4.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 2f);
		
		meshes = new HashMap<Integer, Mesh>(64);
		
		shader = new ShaderProgram(Gdx.files.internal("assets/shader/terrain.vert"),Gdx.files.internal("assets/shader/terrain.frag"));
		
		if(!shader.isCompiled()) {
			throw new GdxRuntimeException(shader.getLog());
		}
		
		System.out.println(shader.getLog());
	}
	
	private Mesh buildTerrain(float[][] material, float grid_size, Vector3 pos) {
		
		//Profiler pA = new Profiler("Terrain mesh: TOTAL", TimeUnit.MICROSECONDS);
		//Profiler pB = new Profiler("Terrain mesh: BUILD", TimeUnit.MICROSECONDS);
		
		//### BEGINNING OF INTERPOLATION ###//
		
		//Profiler pC = new Profiler("Terrain mesh: INTERPOLATE", TimeUnit.MICROSECONDS);
		
		final int MULT = 2;
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
				
				float vLL = v[ iV	][ jV	];
				float vLR = v[ iV+1	][ jV	];
				float vUL = v[ iV	][ jV+1	];
				float vUR = v[ iV+1	][ jV+1	];
				
				u[i][j] = (vUL*dj/MULTF+vLL*(1f-dj/MULTF))*(1f-di/MULTF) + (vUR*dj/MULTF+vLR*(1f-dj/MULTF))*(di/MULTF);
			}
		}
		
		material = u;
		
		//pC.log();
		
		//### END OF INTERPOLATION ###//
		
		final int x_length = material[0].length;
		final int y_length = material.length;
		
		final VertexAttributes vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
		
		final float[] vertices	= new float[x_length * y_length*(vertexAttributes.vertexSize / 4)];
		final short[] indices	= new short[6 * (x_length - 1) * (y_length - 1)];
		
		//This scale looks good
		final float texture_scale = 40f; //40f
		
		final float x_space = grid_size  / MULTF;
		final float y_space = grid_size  / MULTF; // * MathUtils.sin(MathUtils.PI * 0.25f);
		
		int k = 0;
		for (int i = 0; i < y_length; i++) {
			for (int j = 0; j < x_length; j++) {
				vertices[k++] = j * x_space;
				vertices[k++] = i * y_space;
				vertices[k++] = 0f;
				vertices[k++] = 0f;
				vertices[k++] = 0f;
				vertices[k++] = 0f;
				vertices[k++] = material[y_length-i-1][j];
				vertices[k++] = ( i * y_space + pos.y ) * texture_scale;
				vertices[k++] = ( j * x_space + pos.x ) * texture_scale;
			}
		}
		
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
		final Mesh mesh = meshPool.obtain();
		
		System.out.println("Vertices: " + vertices.length);
		System.out.println("Indices: " + indices.length);
		
		System.out.println("Vertices-size: " + mesh.getVerticesBuffer().capacity() * 4 / 1024 + " KB");
		System.out.println("Indices-size: " + mesh.getIndicesBuffer().capacity() * 2 / 1024 + " KB");

		mesh.setVertices(vertices, 0,vertices.length);
		mesh.setIndices(indices, 0, indices.length);
		
		//pD.log();
		
		//pA.log();
		
		//System.out.println();
		
		return mesh;
	}

	Profiler pInserted = new Profiler("Terrain-Insert", TimeUnit.MICROSECONDS);
	Profiler pRemoved = new Profiler("Terrain-Remove", TimeUnit.MICROSECONDS);
	
	@Override
	protected void inserted(int entityId) {
		
		//if(meshes.containsKey(entityId)) return;
		
		AbsolutePosition ap = apm.get(entityId);
		TerrainHeightField thf = thfm.get(entityId);
		
		Mesh mesh = buildTerrain(thf.height, thf.grid_size, ap.position);
		
		meshes.put(entityId, mesh);
	}
	
	@Override
	protected void removed(int entityId) {
		
		Mesh mesh = meshes.remove(entityId);
		
		if(mesh != null) {
			meshPool.free(mesh);
		}
	}
	
	@Override
	protected void begin() {
		
		profiler.start();
		
		time = (float)( TimeSystem.getCurrentTimeSeconds() % ( MathUtils.PI * 100f ) );

		tex4.bind(4);
		tex3.bind(3);
		tex2.bind(2);
		tex1.bind(1);
		tex0.bind(0); //bind first texture unit last so that it is the active texture unit again!
		
		shader.begin();

		shader.setUniformMatrix("u_projTrans", camera.combined);
		
		if(shader.hasUniform("time")) {
			shader.setUniformf("time", time);
		}

		shader.setUniformi("u_tex0", 0);
		shader.setUniformi("u_tex1", 1);
		shader.setUniformi("u_tex2", 2);
		shader.setUniformi("u_tex3", 3);
		shader.setUniformi("u_tex4", 4);
	}

	@Override
	protected void process(int entityId) {
		
		Vector3 ap = apm.get(entityId).position;
		TerrainHeightField thf = thfm.get(entityId);
		
		if(thf.needsMeshRebuild) {
			
			thf.needsMeshRebuild = false;
			
			removed(entityId);
			inserted(entityId);
		}

		shader.setUniform2fv("u_offset", new float[] {Math2D.round(ap.x, Metrics.ppm), Math2D.round(ap.y, Metrics.ppm)}, 0, 2);

		Mesh mesh = meshes.get(entityId);
		
		mesh.render(shader, GL20.GL_TRIANGLES);
	}
	
	@Override
	protected void end() {

		shader.end();
		
		profiler.stop();
		
		//profiler.log();
	}
	
	@Override
	protected void dispose() {

		for(Mesh mesh : meshes.values()) {
			mesh.dispose();
		}

		meshes.clear();
		
		shader.dispose();
		
		tex0.dispose();
		tex1.dispose();
		tex2.dispose();
		tex3.dispose();
		tex4.dispose();
	}
}
