package de.vatterger.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import de.vatterger.engine.util.Metrics;

public class ShaderTerrainTest extends Game {

	private Texture tex0;
	private Texture tex1;
	private Texture tex2;
	
	private Mesh mesh;

	private ShaderProgram shader;
	
	private OrthographicCamera camera;
	private ScalingViewport viewport;
	
	@Override
	public void create() {
		tex0 = new Texture(Gdx.files.internal("assets/texture/water.png"),true);
		tex0.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex0.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8f);

		tex1 = new Texture(Gdx.files.internal("assets/texture/sand.png"),true);
		tex1.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex1.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8f);
		
		tex2 = new Texture(Gdx.files.internal("assets/texture/grass.png"),true);
		tex2.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tex2.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8f);

		float m[][] = {
				{1f,0f,0f,1f},
				{1f,0f,0f,1f}
		};
		 
		
		mesh = buildTerrain(m);

		shader = new ShaderProgram(Gdx.files.internal("assets/shader/terrain.vert"),Gdx.files.internal("assets/shader/terrain.frag"));
		System.out.println(shader.getLog());
		
		camera = new OrthographicCamera();
		viewport = new ScalingViewport(Scaling.fit, Metrics.ww , Metrics.hw, camera);
		camera.near = 10;
		camera.far = 2000;
		camera.translate(new Vector3(0, 100f, 100f));
		camera.lookAt(0, 0, 0);
		camera.update();
	}
	
	private Mesh buildTerrain(float[][] material) {
		VertexAttributes vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
		
		MeshBuilder builder = new MeshBuilder();
		builder.begin(vertexAttributes, GL20.GL_TRIANGLES);
		
		int x_length = material[0].length;
		int y_length = material.length;
		
		float x_space = 20f;
		float y_space = 20f;
		
		float texture_scale = 10f;
		
		for (int i = 0; i < y_length; i++) {
			for (int j = 0; j < x_length; j++) {
				builder.vertex(j*x_space,i*y_space,0, 0,0,0,material[y_length-i-1][j]+MathUtils.sin(time/20), i*x_space/texture_scale,j*y_space/texture_scale);
			}
		}
		
		for (int i = 0; i < y_length-1; i++) {
			for (int j = 0; j < x_length-1; j++) {
				builder.index((short)(i*x_length+j),(short)(i*x_length+j+1),(short)(i*x_length+j+x_length));
				builder.index((short)(i*x_length+j+1),(short)(i*x_length+j+1+x_length),(short)(i*x_length+j+x_length));
			}
		}

		return builder.end();
	}
	
	private float time = 0f;
	
	@Override
	public void render() {
		if(Gdx.input.justTouched() || true) {
			/*float m[][] = new float[16][16];
			
			for (int i = 0; i < m.length; i++) {
				for (int j = 0; j < m[0].length; j++) {
					m[i][j] = Math.min(1f,MathUtils.random(0f,1f));
				}
			}*/

			float m[][] = {
					{0.75f,0f,0f,0.75f},
					{1f,0f,0f,1f},
					{0.5f,0.25f,0f,0.6f},
					{0.25f,0f,0f,0.5f},
					{1f,0f,0f,1f},
					{0.75f,0f,0f,0.75f}
			};
			 

			mesh = buildTerrain(m);
		}
		
		Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()) + " - " + (int)((1f/Gdx.graphics.getRawDeltaTime()) + 0.5f));
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		
		tex0.bind(0);
		tex1.bind(1);
		tex2.bind(2);
		
		shader.begin();
		shader.setUniformMatrix("u_projTrans", camera.combined);
		shader.setUniform2fv("u_offset", new float[]{-20f,-20f}, 0, 2);
		shader.setUniformf("time", time);
		time += Gdx.graphics.getRawDeltaTime();
		time = time % (MathUtils.PI * 100f);

		shader.setUniformi("u_tex0", 0);
		shader.setUniformi("u_tex1", 1);
		shader.setUniformi("u_tex2", 2);
		
		mesh.render(shader, GL20.GL_TRIANGLES);
		
		shader.end();
	}

	@Override
	public void resize(int width, int height) {
		Metrics.wv = width;
		Metrics.hv = height;
		
		Metrics.ww = Metrics.wv * Metrics.mpp;
		Metrics.hw = Metrics.hv * Metrics.mpp;
		
		viewport.setWorldSize(Metrics.ww , Metrics.hw);
		viewport.update(Metrics.wv, Metrics.hv, false);
	}

	public static void main(String[] args) {
		LwjglApplicationConfiguration configWindow = new LwjglApplicationConfiguration();

		configWindow.title = "ShaderTerrainTest";

		configWindow.fullscreen = false;
		configWindow.vSyncEnabled = false;
		configWindow.resizable = false;

		configWindow.width = 640;
		configWindow.height = 480;

		configWindow.foregroundFPS = 60;
		configWindow.backgroundFPS = 30;

		configWindow.addIcon("icon32.png", FileType.Internal);

		new LwjglApplication(new ShaderTerrainTest(), configWindow);
	}
}
