package com.badlogic.gdx.graphics.g2d;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

/** Draws batched quads using indices. Maintains an LRU texture-cache to combine draw calls with different textures effectively.
 * @see Batch
 * 
 * @author mzechner
 * @author Nathan Sweet
 * @author VaTTeRGeR */

public class TextureArraySpriteBatch implements Batch {

	private int idx = 0;
	
	private final Mesh mesh;

	private final float[] vertices;
	
	private final int spriteVertexSize = Sprite.VERTEX_SIZE;
	
	//CHANGE: The maximum number of available texture units for the fragment shader
	private final int maxTextureUnits;
	//CHANGE: Textures in use (index: Texture Unit, value: Texture)
	private final Array<Texture> usedTextures;
	//CHANGE: LRU Array (first item = LRU) (index: Position in LRU order, value: Texture Unit)
	private final IntArray usedTexturesLRU;
	//CHANGE: Gets sent to the fragment shader as an uniform "uniform sampler2d[X] u_texture"
	private final IntBuffer textureUnitIndicesBuffer;

	private boolean drawing = false;

	private final Matrix4 transformMatrix = new Matrix4();
	private final Matrix4 projectionMatrix = new Matrix4();
	private final Matrix4 combinedMatrix = new Matrix4();

	private boolean blendingDisabled = false;
	private int blendSrcFunc = GL20.GL_SRC_ALPHA;
	private int blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
	private int blendSrcFuncAlpha = GL20.GL_SRC_ALPHA;
	private int blendDstFuncAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;

	private final ShaderProgram shader;

	private final Color color = new Color(1, 1, 1, 1);
	private float colorPacked = Color.WHITE_FLOAT_BITS;

	/** Number of render calls since the last {@link #begin()}. **/
	public int renderCalls = 0;

	/** Number of rendering calls, ever. Will not be reset unless set manually. **/
	public int totalRenderCalls = 0;

	/** The maximum number of sprites rendered in one batch so far. **/
	public int maxSpritesInBatch = 0;

	public TextureArraySpriteBatch() {
		this(1024);
	}
	
	public TextureArraySpriteBatch(int size) {
		this(size, null);
	}
	
	public TextureArraySpriteBatch(int size, ShaderProgram defaultShader) {
		
		// 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites max.
		if (size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

		//CHANGE: Query the number of available texture units
		IntBuffer texUnitsMaxBuffer = BufferUtils.createIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_IMAGE_UNITS, texUnitsMaxBuffer.position(0));

		maxTextureUnits = texUnitsMaxBuffer.get();
		
		usedTextures = new Array<Texture>(true, maxTextureUnits, Texture.class);
		usedTexturesLRU = new IntArray(true, maxTextureUnits);
		
		//CHANGE: This contains the numbers 0 ... maxTextureUnits-1. We send these to the shader as an uniform.
		textureUnitIndicesBuffer = BufferUtils.createIntBuffer(maxTextureUnits);
		for (int i = 0; i < maxTextureUnits; i++) {
			textureUnitIndicesBuffer.put(i);
			
		}
		textureUnitIndicesBuffer.flip();
		
		VertexDataType vertexDataType = (Gdx.gl30 != null) ? VertexDataType.VertexBufferObjectWithVAO : VertexDataType.VertexArray;

		//CHANGE: The vertex data is extended with one float for the texture index.
		mesh = new Mesh(vertexDataType, false, size * 4, size * 6,
			new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
			new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
			new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
			new VertexAttribute(Usage.Generic, 1, "texture_index"));

		projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		vertices = new float[size * (Sprite.SPRITE_SIZE + 4)];

		int len = size * 6;
		short[] indices = new short[len];
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
			indices[i] = j;
			indices[i + 1] = (short)(j + 1);
			indices[i + 2] = (short)(j + 2);
			indices[i + 3] = (short)(j + 2);
			indices[i + 4] = (short)(j + 3);
			indices[i + 5] = j;
		}
		mesh.setIndices(indices);


		shader = createMultitextureShader(maxTextureUnits);
	}
	
	/** Returns a new instance of the default shader used by SpriteBatch for GL2 when no shader is specified. */
	static public ShaderProgram createMultitextureShader (int maxTextureUnits) {
		
		//CHANGE: The texture index is just passed to the fragment shader, maybe there's an more elegant way.
		String vertexShader = "#version 110\n" //
			+ "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "attribute float texture_index;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "varying float v_texture_index;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
			+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   v_texture_index = texture_index;\n" //
			+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";
		
		//CHANGE: The texture is simply selected from an array of textures
		String fragmentShader = "#version 110\n" //
			+ "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n" //
			+ "varying LOWP vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "varying float v_texture_index;\n" //
			+ "uniform sampler2D u_texture[" + maxTextureUnits + "];\n" //
			+ "void main()\n"//
			+ "{\n" //
			+ "  int index = int(v_texture_index);" //
			+ "  gl_FragColor = v_color * texture2D(u_texture[index], v_texCoords);\n" //
			+ "}";

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		return shader;
	}
	
	@Override
	public void begin() {
		
		if (drawing) throw new IllegalStateException("SpriteBatch.end must be called before begin.");
		renderCalls = 0;

		Gdx.gl.glDepthMask(false);
		
		shader.begin();
		
		setupMatrices();

		drawing = true;
	}

	@Override
	public void end() {

		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before end.");
		
		if (idx > 0) flush();
		
		//CHANGE: Optional, levels the playing field so to say
		usedTextures.clear();
		usedTexturesLRU.clear();
		
		drawing = false;

		GL20 gl = Gdx.gl;
		gl.glDepthMask(true);
		if (isBlendingEnabled()) gl.glDisable(GL20.GL_BLEND);

		shader.end();
	}

	@Override
	public void dispose() {
		mesh.dispose();
		shader.dispose();
	}

	@Override
	public void setColor(Color tint) {
		color.set(tint);
		colorPacked = tint.toFloatBits();
	}

	@Override
	public void setColor(float r, float g, float b, float a) {
		color.set(r, g, b, a);
		colorPacked = color.toFloatBits();
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setPackedColor(float packedColor) {
		Color.abgr8888ToColor(color, packedColor);
		this.colorPacked = packedColor;
	}

	@Override
	public float getPackedColor() {
		return colorPacked;
	}

	@Override
	public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height,
			float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX,
			boolean flipY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
			int srcHeight, boolean flipX, boolean flipY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2,
			float v2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Texture texture, float x, float y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Texture texture, float x, float y, float width, float height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
		
		if (!drawing) {
			throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
		}
		
		// original Sprite attribute size plus one extra float per sprite vertex
		if(vertices.length - idx < count + count / spriteVertexSize) {
			flush();
		}
		
		// Assigns a texture unit to this texture, flushing if none is available
		final float textureUnitIndex = (float)activateTexture(texture);
		
		copyVerticesAndInjectTextureUnit(spriteVertices, count, textureUnitIndex);
	}
	
	private void copyVerticesAndInjectTextureUnit(float[] spriteVertices, int count, float textureUnit) {
		
		// spriteVertexSize is the number of floats an unmodified input vertex consists of.
		for (int srcPos = 0; srcPos < count; srcPos += spriteVertexSize) {
			
			// Copy the vertices
			System.arraycopy(spriteVertices, srcPos, vertices, idx, spriteVertexSize);

			// Advance idx by vertex float count
			idx += spriteVertexSize;
			
			// Inject texture unit index and advance idx
			vertices[idx++] = textureUnit;
		}
	}

	@Override
	public void draw(TextureRegion region, float x, float y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(TextureRegion region, float x, float y, float width, float height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
			float scaleX, float scaleY, float rotation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
			float scaleX, float scaleY, float rotation, boolean clockwise) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(TextureRegion region, float width, float height, Affine2 transform) {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() {
		
		if (idx == 0) return;

		renderCalls++;
		totalRenderCalls++;
		int spritesInBatch = idx / (Sprite.SPRITE_SIZE + 4);
		if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
		int count = spritesInBatch * 6;

		// Bind the textures
		int textureUnit = 0;
		for (Texture texture : usedTextures) {
			texture.bind(textureUnit++);
		}
		// Set Texture unit one as active again before drawing.
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		Mesh mesh = this.mesh;
		mesh.setVertices(vertices, 0, idx);
		mesh.getIndicesBuffer().position(0);
		mesh.getIndicesBuffer().limit(count);

		if (blendingDisabled) {
			Gdx.gl.glDisable(GL20.GL_BLEND);
		} else {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			if (blendSrcFunc != -1) Gdx.gl.glBlendFuncSeparate(blendSrcFunc, blendDstFunc, blendSrcFuncAlpha, blendDstFuncAlpha);
		}

		mesh.render(shader, GL20.GL_TRIANGLES, 0, count);

		idx = 0;
	}

	//CHANGE: Assigns Texture units and manages the LRU cache.
	private int activateTexture(Texture texture) {
		
		// This is our identifier for the textures, you could also use something else
		final int textureHandle = texture.getTextureObjectHandle();
		
		// First try to see if the texture is already in use
		for (int i = 0; i < usedTextures.size; i++) {
			
			if(textureHandle == usedTextures.get(i).getTextureObjectHandle()) {
				
				// Position this texture as the most recently used one.
				// Optimization: Skip the copying if slot i is already the most recently used slot
				if(usedTexturesLRU.get(usedTexturesLRU.size - 1) != i) {
					usedTexturesLRU.removeValue(i);
					usedTexturesLRU.add(i);
				}
				
				return i;
			}
		}
		
		// If a free texture unit is available we just use it
		// If not we have to flush and then throw out the oldest one.
		if(usedTextures.size < usedTextures.items.length) {
			
			final int slot = usedTextures.size;
			
			//System.out.println("Adding new Texture " + textureHandle + " to slot " + usedTextures.size);
			
			usedTextures.add(texture);
			
			// Position this texture as the most recently used one.
			usedTexturesLRU.add(slot);
			
			return slot;
			
		} else {
			
			// We have to flush if there is something in the pipeline already,
			// otherwise the texture indices of previous sprites may be affected
			if(idx > 0) {
				flush();
			}
			
			// The least recently used texture gets kicked out.
			final int slot = usedTexturesLRU.first();
			
			//System.out.println("Kicking out Texture from slot " + slot + " for texture " + textureHandle);

			usedTextures.set(slot, texture);
			
			// Position this texture as the most recently used one.
			usedTexturesLRU.removeIndex(0);
			usedTexturesLRU.add(slot);
			
			return slot;
		}
	}
	
	@Override
	public void disableBlending() {
		if (blendingDisabled) return;
		flush();
		blendingDisabled = true;
	}

	@Override
	public void enableBlending() {
		
		if (!blendingDisabled) {
			return;
		}
		
		flush();
		
		blendingDisabled = false;
	}

	@Override
	public void setBlendFunction(int srcFunc, int dstFunc) {
		setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
	}

	@Override
	public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
		
		if (blendSrcFunc == srcFuncColor && blendDstFunc == dstFuncColor && blendSrcFuncAlpha == srcFuncAlpha && blendDstFuncAlpha == dstFuncAlpha) {
			return;
		}
		
		flush();
		
		blendSrcFunc = srcFuncColor;
		blendDstFunc = dstFuncColor;
		blendSrcFuncAlpha = srcFuncAlpha;
		blendDstFuncAlpha = dstFuncAlpha;
	}

	@Override
	public int getBlendSrcFunc() {
		return blendSrcFunc;
	}

	@Override
	public int getBlendDstFunc() {
		return blendDstFunc;
	}

	@Override
	public int getBlendSrcFuncAlpha() {
		return blendSrcFuncAlpha;
	}

	@Override
	public int getBlendDstFuncAlpha() {
		return blendDstFuncAlpha;
	}

	@Override
	public boolean isBlendingEnabled() {
		return !blendingDisabled;
	}

	@Override
	public boolean isDrawing() {
		return drawing;
	}

	@Override
	public Matrix4 getProjectionMatrix() {
		return projectionMatrix;
	}

	@Override
	public Matrix4 getTransformMatrix() {
		return transformMatrix;
	}

	@Override
	public void setProjectionMatrix(Matrix4 projection) {
		
		if (drawing) {
			flush();
		}

		projectionMatrix.set(projection);
		
		if (drawing) {
			setupMatrices();
		}
	}

	@Override
	public void setTransformMatrix(Matrix4 transform) {
		
		if (drawing) {
			flush();
		}

		transformMatrix.set(transform);
		
		if (drawing) {
			setupMatrices();
		}
	}

	private void setupMatrices () {
		
		combinedMatrix.set(projectionMatrix).mul(transformMatrix);
		
		shader.setUniformMatrix("u_projTrans", combinedMatrix);
		
		//CHANGE: Pass the texture index array as an uniform
		Gdx.gl20.glUniform1iv(shader.fetchUniformLocation("u_texture", true), maxTextureUnits, textureUnitIndicesBuffer);
	}

	@Override
	public void setShader(ShaderProgram shader) {
		throw new IllegalAccessError("This batch does not allow custom shaders yet.");
	}

	@Override
	public ShaderProgram getShader() {
		return shader;
	}
}
