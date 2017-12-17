package de.vatterger.game.systems.graphics;

import java.util.concurrent.TimeUnit;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
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

import de.vatterger.engine.util.Profiler;

public class TerrainRenderSystem extends IteratingSystem {

	private Texture tex0;
	private Texture tex1;
	private Texture tex2;
	
	private Mesh mesh;

	private ShaderProgram shader;
	
	private OrthographicCamera camera;
	
	private float time = 0f;

	@SuppressWarnings("unchecked")
	public TerrainRenderSystem(Camera camera) {
		super(Aspect.all());
		this.camera = (OrthographicCamera)camera;
	}
	
	@Override
	protected void initialize() {
		tex0 = new Texture(Gdx.files.internal("assets/texture/water1.png"),true);
		tex0.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex0.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8f);

		tex1 = new Texture(Gdx.files.internal("assets/texture/sand1.png"),true);
		tex1.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex1.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8f);
		
		tex2 = new Texture(Gdx.files.internal("assets/texture/dirt2.png"),true);
		tex2.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex2.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8f);
		
		shader = new ShaderProgram(Gdx.files.internal("assets/shader/terrain.vert"),Gdx.files.internal("assets/shader/terrain.frag"));
		System.out.println(shader.getLog());

		float m[][] = new float[3][3];
		
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++) {
				m[i][j] = MathUtils.random(0f, 1f);
			}
		}

		mesh = buildTerrain(m);
	}
	
	private Mesh buildTerrain(float[][] material) {
		Profiler pA = new Profiler("Mesh build ALL", TimeUnit.MICROSECONDS);
		Profiler pB = new Profiler("Mesh build calculate", TimeUnit.MICROSECONDS);
		
		VertexAttributes vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
		
		float x_space = 10f;
		float y_space = x_space;
		
		int x_length = material[0].length;
		int y_length = material.length;
		
		float texture_scale = 1f;
		
		float[] vertices	= new float[x_length*y_length*(vertexAttributes.vertexSize/4)];
		short[] indices		= new short[2 * 6 * (x_length - 1) * (y_length - 1)];
		
		int k = 0;
		for (int i = 0; i < y_length; i++) {
			for (int j = 0; j < x_length; j++) {
				vertices[k++] = j*x_space;
				vertices[k++] = i*y_space;
				vertices[k++] = 0;
				vertices[k++] = 0;
				vertices[k++] = 0;
				vertices[k++] = 0;
				vertices[k++] = material[y_length-i-1][j];
				vertices[k++] = i*texture_scale;
				vertices[k++] = j*texture_scale;
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

		pB.log();
		
		Profiler pC = new Profiler("Mesh build SET", TimeUnit.MICROSECONDS);
		
		Mesh mesh = new Mesh(true, x_length*y_length, 2 * 6 * (x_length - 1) * (y_length - 1), vertexAttributes);

		mesh.setVertices(vertices);
		mesh.setIndices(indices);
		
		pC.log();
		
		pA.log();
		
		System.out.println();
		
		return mesh;
	}

	@Override
	protected void inserted(int entityId) {
	}
	
	@Override
	protected void removed(int entityId) {
	}
	
	@Override
	protected void begin() {
		time += Gdx.graphics.getDeltaTime();
		time = time % (MathUtils.PI * 100f);

		tex2.bind(2);
		tex1.bind(1);
		tex0.bind(0);
		
		shader.begin();
		
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				shader.setUniformMatrix("u_projTrans", camera.combined.cpy().rotate(Vector3.X, -45));
				shader.setUniform2fv("u_offset", new float[] { ((float)i) * 20f, ((float)j) * 20f }, 0, 2);
				shader.setUniformf("time", time);

				shader.setUniformi("u_tex0", 0);
				shader.setUniformi("u_tex1", 1);
				shader.setUniformi("u_tex2", 2);

				mesh.render(shader, GL20.GL_TRIANGLES);
			}
		}
		
		shader.end();
	}

	@Override
	protected void process(int e) {
	}
	
	@Override
	protected void dispose() {
		mesh.dispose();
		
		shader.dispose();
		
		tex0.dispose();
		tex1.dispose();
		tex2.dispose();
	}
}
