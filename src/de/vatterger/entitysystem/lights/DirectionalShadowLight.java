package de.vatterger.entitysystem.lights;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.ShadowMap;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

 /* @author Xoppa */
public class DirectionalShadowLight extends DirectionalLight implements ShadowMap, Disposable {
	protected FrameBuffer fbo;
	protected Camera cam;
	protected float halfDepth;
	protected float halfHeight;
	protected final Vector3 tmpV = new Vector3();
	protected final TextureDescriptor textureDesc;

	public DirectionalShadowLight (int shadowMapWidth, int shadowMapHeight, float shadowViewportWidth, float shadowViewportHeight,
		float shadowNear, float shadowFar) {
		fbo = new FrameBuffer(Format.RGBA8888, shadowMapWidth, shadowMapHeight, true);
		cam = new OrthographicCamera(shadowViewportWidth, shadowViewportHeight);
		cam.near = shadowNear;
		cam.far = shadowFar;
		halfHeight = shadowViewportHeight * 0.5f;
		halfDepth = shadowNear + 0.5f * (shadowFar - shadowNear);
		textureDesc = new TextureDescriptor();
		textureDesc.minFilter = textureDesc.magFilter = Texture.TextureFilter.Nearest;
		textureDesc.uWrap = textureDesc.vWrap = Texture.TextureWrap.ClampToEdge;
	}

	public void update (final Camera camera) {
		update(tmpV.set(camera.direction).scl(halfHeight), camera.direction);
	}

	public void update (final Vector3 center, final Vector3 forward) {
		// cam.position.set(10,10,10);
		cam.position.set(direction).scl(-halfDepth).add(center);
		cam.direction.set(direction).nor();
		// cam.up.set(forward).nor();
		cam.normalizeUp();
		cam.update();
	}

	public void begin (final Camera camera) {
		update(camera);
		begin();
	}

	public void begin (final Vector3 center, final Vector3 forward) {
		update(center, forward);
		begin();
	}

	public void begin () {
		final int w = fbo.getWidth();
		final int h = fbo.getHeight();
		fbo.begin();
		Gdx.gl.glViewport(0, 0, w, h);
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
		Gdx.gl.glScissor(1, 1, w - 2, h - 2);
	}

	public void end () {
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		fbo.end();
	}

	public FrameBuffer getFrameBuffer () {
		return fbo;
	}

	public Camera getCamera () {
		return cam;
	}

	@Override
	public Matrix4 getProjViewTrans () {
		return cam.combined;
	}

	@Override
	public TextureDescriptor getDepthMap () {
		textureDesc.texture = fbo.getColorBufferTexture();
		return textureDesc;
	}

	@Override
	public void dispose () {
		if (fbo != null) fbo.dispose();
		fbo = null;
	}
}
