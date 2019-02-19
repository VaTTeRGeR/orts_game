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
	
	private HashMap<Integer,Mesh> meshes;

	private ShaderProgram shader;
	
	private float time = 0f;

	private Profiler profiler = new Profiler("TerrainRenderSystem", TimeUnit.MICROSECONDS);

	@SuppressWarnings("unchecked")
	public TerrainRenderSystem() {
		super(Aspect.all(AbsolutePosition.class,TerrainHeightField.class).exclude(Culled.class));
	}
	
	@Override
	protected void initialize() {
		
		GraphicalProfilerSystem.registerProfiler("TerrainRender", Color.RED, profiler);
		
		//System.out.println("abs: " + Gdx.files.internal("assets/texture/sand1.png").file().getAbsolutePath());
		//System.out.println("exists: " + Gdx.files.internal("assets/texture/sand1.png").file().exists());
		
		tex0 = new Texture(Gdx.files.internal("assets/texture/water1.png"), true);
		tex0.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex0.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 2f);

		tex1 = new Texture(Gdx.files.internal("assets/texture/sand1.png"), true);
		tex1.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex1.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 2f);
		
		tex2 = new Texture(Gdx.files.internal("assets/texture/grass4.png"), true);
		tex2.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex2.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 2f);
		
		meshes = new HashMap<Integer, Mesh>(64);
		
		shader = new ShaderProgram(Gdx.files.internal("assets/shader/terrain.vert"),Gdx.files.internal("assets/shader/terrain.frag"));
		
		if(!shader.isCompiled()) {
			throw new GdxRuntimeException(shader.getLog());
		}

		System.out.println(shader.getLog());
	}
	
	private Mesh buildTerrain(float[][] material, float grid_size, float scale) {
		
		//Profiler pA = new Profiler("Terrain mesh: TOTAL", TimeUnit.MICROSECONDS);
		//Profiler pB = new Profiler("Terrain mesh: BUILD", TimeUnit.MICROSECONDS);
		
		//### BEGINNING OF INTERPOLATION ###//

		//Profiler pC = new Profiler("Terrain mesh: INTERPOLATE", TimeUnit.MICROSECONDS);
		
		final int mult = 3;
		final float multf = (float) mult;
		
		//The original data
		float v[][] = material;
		
		//The interpolated data
		float u[][] = new float[(material.length-1) * mult + 1][(material[0].length-1) * mult + 1];
		
		for (int i = 0; i < u.length; i++) {
			for (int j = 0; j < u[0].length; j++) {
				int iV = i/mult;
				int jV = j/mult;

				if(i==u.length-1) {
					iV--;
				}

				if(j==u[0].length-1) {
					jV--;
				}

				int iR = iV*mult;
				int jR = jV*mult;
				
				float di = i-iR;
				float dj = j-jR;
				
				float vLL = v[iV]   [jV];
				float vLR = v[iV+1] [jV];
				float vUL = v[iV]   [jV+1];
				float vUR = v[iV+1] [jV+1];
				
				u[i][j] = (vUL*dj/multf+vLL*(1f-dj/multf))*(1f-di/multf) + (vUR*dj/multf+vLR*(1f-dj/multf))*(di/multf);
			}
		}
		
		material = u;
		
		//pC.log();
		
		//### END OF INTERPOLATION ###//
		
		final int x_length = material[0].length;
		final int y_length = material.length;
		
		final VertexAttributes vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
		
		final float x_space = grid_size * scale / multf;
		final float y_space = x_space * MathUtils.sin(MathUtils.PI*0.25f);
		
		
		final float texture_scale = 40f; //40f
		
		final float[] vertices	= new float[x_length*y_length*(vertexAttributes.vertexSize/4)];
		final short[] indices	= new short[2 * 6 * (x_length - 1) * (y_length - 1)];

		
		int k = 0;
		for (int i = 0; i < y_length; i++) {
			for (int j = 0; j < x_length; j++) {
				vertices[k++] = j*x_space;
				vertices[k++] = i*y_space;
				vertices[k++] = 0f;
				vertices[k++] = 0f;
				vertices[k++] = 0f;
				vertices[k++] = 0f;
				vertices[k++] = material[y_length-i-1][j];
				vertices[k++] = i*(texture_scale*x_space);
				vertices[k++] = j*(texture_scale*x_space);
			}
		}
		
		k = 0;
		for (int i = 0; i < y_length-1; i++) {
			for (int j = 0; j < x_length-1; j++) {
				indices[k++] = (short)(i*x_length+j);
				indices[k++] = (short)(i*x_length+j+1);
				indices[k++] = (short)(i*x_length+j+x_length);
				indices[k++] = (short)(i*x_length+j+1);
				indices[k++] = (short)(i*x_length+j+1+x_length);
				indices[k++] = (short)(i*x_length+j+x_length);
			}
		}

		//pB.log();
		
		//Profiler pD = new Profiler("Terrain mesh: UPLOAD", TimeUnit.MICROSECONDS);
		
		final Mesh mesh = new Mesh(true, x_length*y_length, 2 * 6 * (x_length - 1) * (y_length - 1), vertexAttributes);

		mesh.setVertices(vertices);
		mesh.setIndices(indices);
		
		//pD.log();
		
		//pA.log();
		
		//System.out.println();
		
		return mesh;
	}

	@Override
	protected void inserted(int entityId) {
		
		TerrainHeightField thf = thfm.get(entityId);
		
		meshes.put(entityId, buildTerrain(thf.height, thf.grid_size, 1f));
	}
	
	@Override
	protected void removed(int entityId) {
		
		Mesh mesh = meshes.remove(entityId);
		
		if(mesh != null) {
			mesh.dispose();
		}
	}
	
	@Override
	protected void begin() {
		
		profiler.start();
		
		time = (float)(TimeSystem.getCurrentTimeSeconds() % (MathUtils.PI * 100d));

		shader.begin();
		
		tex2.bind(2);
		tex1.bind(1);
		tex0.bind(0); //bind first texture unit last so that it is the active texture unit again!
		
		shader.setUniformMatrix("u_projTrans", camera.combined.cpy());
		
		if(shader.hasUniform("time")) {
			shader.setUniformf("time", time);
		}

		shader.setUniformi("u_tex0", 0);
		shader.setUniformi("u_tex1", 1);
		shader.setUniformi("u_tex2", 2);

	}

	@Override
	protected void process(int entityId) {

		Vector3 ap = apm.get(entityId).position;
		shader.setUniform2fv("u_offset", new float[] { (ap.x), (ap.y) * MathUtils.sin(MathUtils.PI / 4f) }, 0, 2);
		
		meshes.get(entityId).render(shader, GL20.GL_TRIANGLES);
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
	}
}
