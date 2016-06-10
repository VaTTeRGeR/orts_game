package de.vatterger.techdemo.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 * @author Xoppa
 */
public final class TestShader implements Shader {
	private	ShaderProgram	program;
	private	RenderContext	context;
	private	int		u_projTrans;
	private	int		u_worldTrans;
	private	int		u_time;
	private	float	u_time_VALUE;
	
	
	public void updateTime(float delta) {
		u_time_VALUE += delta;
	}

	@Override
	public void init() {
        final String vert = Gdx.files.internal("custom.vertex").readString();
        final String frag = Gdx.files.internal("custom.fragment").readString();

        program = new ShaderProgram(vert, frag);
        
        if (!program.isCompiled())
        	throw new GdxRuntimeException(program.getLog());

        u_projTrans 	= program.getUniformLocation("u_projViewTrans");
        u_worldTrans 	= program.getUniformLocation("u_worldTrans");
        u_time			= program.getUniformLocation("u_time");
	}

	@Override
	public void dispose() {
		program.dispose();
	}

	@Override
	public void begin(Camera camera, RenderContext context) {
		this.context = context;
		program.begin();
		program.setUniformMatrix(u_projTrans, camera.combined);
		program.setUniformf(u_time, u_time_VALUE);
		context.setDepthTest(GL20.GL_LEQUAL);
		context.setCullFace(GL20.GL_BACK);
		//context.setBlending(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	@Override
	public void render(Renderable renderable) {
		((TextureAttribute)renderable.material.get(TextureAttribute.Diffuse)).textureDescription.texture.bind();
		program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
		renderable.meshPart.render(program);
	}

	@Override
	public void end() {
		program.end();
	}

	@Override
	public int compareTo(Shader other) {
		return 0;
	}
	@Override
	public boolean canRender(Renderable instance) {
		return true;
	}
}