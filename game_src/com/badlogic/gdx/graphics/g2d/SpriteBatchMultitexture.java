/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

/** Draws batched quads using indices.
 * @see Batch
 * @author mzechner
 * @author Nathan Sweet
 * @author VaTTeRGeR */
public class SpriteBatchMultitexture {

	private Mesh mesh;

	int idx = 0;
	
	final float[] vertices;
	
	//CHANGE: The maximum number of available texture units for the fragment shader
	private final int maxTextureUnits;
	//CHANGE: Textures in use (index: Texture Unit, value: Texture)
	private final Array<Texture> usedTextures;
	//CHANGE: LRU Array (first item = LRU) (index: Position in LRU order, value: Texture Unit)
	private final IntArray usedTexturesLRU;
	//CHANGE: Gets sent to the fragment shader as an uniform "uniform sampler2d[X] u_texture"
	private final IntBuffer textureUnitIndicesBuffer;

	boolean drawing = false;

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
	float colorPacked = Color.WHITE_FLOAT_BITS;

	/** Number of render calls since the last {@link #begin()}. **/
	public int renderCalls = 0;

	/** Number of rendering calls, ever. Will not be reset unless set manually. **/
	public int totalRenderCalls = 0;

	/** The maximum number of sprites rendered in one batch so far. **/
	public int maxSpritesInBatch = 0;

	/** Constructs a new SpriteBatch with a size of 1000, one buffer, and the default shader.
	 * @see SpriteBatchMultitexture#SpriteBatch(int, ShaderProgram) */
	public SpriteBatchMultitexture () {
		this(1000, null);
	}

	/** Constructs a SpriteBatch with one buffer and the default shader.
	 * @see SpriteBatchMultitexture#SpriteBatch(int, ShaderProgram) */
	public SpriteBatchMultitexture (int size) {
		this(size, null);
	}

	/** Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
	 * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
	 * respect to the current screen resolution.
	 * <p>
	 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
	 * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See {@link #createDefaultShader()}.
	 * @param size The max number of sprites in a single batch. Max of 8191.
	 * @param defaultShader The default shader to use. This is not owned by the SpriteBatch and must be disposed separately. */
	public SpriteBatchMultitexture (int size, ShaderProgram defaultShader) {
		// 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites max.
		if (size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

		//CHANGE: Query the number of available texture units
		IntBuffer texUnitsMaxBuffer = BufferUtils.createIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_IMAGE_UNITS, texUnitsMaxBuffer.position(0));

		//CHANGE: Arbitrary limit of 16 TUs -> less work when shuffling the LRU Array
		maxTextureUnits = Math.min(texUnitsMaxBuffer.get(), 16);
		
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

		vertices = new float[size * SpriteMultitexture.SPRITE_SIZE];

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
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
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
		String fragmentShader = "#ifdef GL_ES\n" //
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

	public void begin () {
		if (drawing) throw new IllegalStateException("SpriteBatch.end must be called before begin.");
		renderCalls = 0;

		Gdx.gl.glDepthMask(false);
		
		shader.begin();
		
		setupMatrices();

		drawing = true;
	}

	public void end () {

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

	
	public void setColor (Color tint) {
		color.set(tint);
		colorPacked = tint.toFloatBits();
	}

	
	public void setColor (float r, float g, float b, float a) {
		color.set(r, g, b, a);
		colorPacked = color.toFloatBits();
	}

	
	public Color getColor () {
		return color;
	}

	
	public void setPackedColor (float packedColor) {
		Color.abgr8888ToColor(color, packedColor);
		this.colorPacked = packedColor;
	}

	
	public float getPackedColor () {
		return colorPacked;
	}
	
	public void draw (Texture texture, float[] spriteVertices, int offset, int count) {
		
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		int verticesLength = vertices.length;
		int remainingVertices = verticesLength;
		
		//CHANGE: Grab an texture unit index, will flush if all texture units are used already
		final float textureUnit = (float)activateTexture(texture);
		
		//CHANGE: Insert the texture unit index into the vertex data
		spriteVertices[SpriteMultitexture.T1] = textureUnit;
		spriteVertices[SpriteMultitexture.T2] = textureUnit;
		spriteVertices[SpriteMultitexture.T3] = textureUnit;
		spriteVertices[SpriteMultitexture.T4] = textureUnit;
		
		remainingVertices -= idx;
		if (remainingVertices == 0) {
			flush();
			remainingVertices = verticesLength;
		}
		
		int copyCount = Math.min(remainingVertices, count);
		System.arraycopy(spriteVertices, offset, vertices, idx, copyCount);
		
		idx += copyCount;
		count -= copyCount;
		
		while (count > 0) {
			offset += copyCount;
			flush();
			copyCount = Math.min(verticesLength, count);
			System.arraycopy(spriteVertices, offset, vertices, 0, copyCount);
			idx += copyCount;
			count -= copyCount;
		}
	}

	public void flush () {
		
		if (idx == 0) return;

		renderCalls++;
		totalRenderCalls++;
		int spritesInBatch = idx / SpriteMultitexture.SPRITE_SIZE;
		if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
		int count = spritesInBatch * 6;

		//CHANGE: Bind the textures
		int textureUnit = 0;
		for (Texture texture : usedTextures) {
			texture.bind(textureUnit++);
		}
		//CHANGE: Set Texture unit one as active again before drawing.
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

	
	public void disableBlending () {
		if (blendingDisabled) return;
		flush();
		blendingDisabled = true;
	}

	
	public void enableBlending () {
		if (!blendingDisabled) return;
		flush();
		blendingDisabled = false;
	}

	
	public void setBlendFunction (int srcFunc, int dstFunc) {
		setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
	}

	
	public void setBlendFunctionSeparate (int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
		if (blendSrcFunc == srcFuncColor && blendDstFunc == dstFuncColor && blendSrcFuncAlpha == srcFuncAlpha
			&& blendDstFuncAlpha == dstFuncAlpha) return;
		flush();
		blendSrcFunc = srcFuncColor;
		blendDstFunc = dstFuncColor;
		blendSrcFuncAlpha = srcFuncAlpha;
		blendDstFuncAlpha = dstFuncAlpha;
	}

	
	public int getBlendSrcFunc () {
		return blendSrcFunc;
	}

	
	public int getBlendDstFunc () {
		return blendDstFunc;
	}

	
	public int getBlendSrcFuncAlpha () {
		return blendSrcFuncAlpha;
	}

	
	public int getBlendDstFuncAlpha () {
		return blendDstFuncAlpha;
	}

	
	public void dispose () {
		mesh.dispose();
		shader.dispose();
	}

	
	public Matrix4 getProjectionMatrix () {
		return projectionMatrix;
	}

	
	public Matrix4 getTransformMatrix () {
		return transformMatrix;
	}

	
	public void setProjectionMatrix (Matrix4 projection) {
		if (drawing) flush();
		projectionMatrix.set(projection);
		if (drawing) setupMatrices();
	}

	
	public void setTransformMatrix (Matrix4 transform) {
		if (drawing) flush();
		transformMatrix.set(transform);
		if (drawing) setupMatrices();
	}
	
	private void setupMatrices () {
		
		combinedMatrix.set(projectionMatrix).mul(transformMatrix);
		
		shader.setUniformMatrix("u_projTrans", combinedMatrix);
		
		//CHANGE: Pass the texture index array as an uniform
		Gdx.gl20.glUniform1iv(shader.fetchUniformLocation("u_texture", true), maxTextureUnits, textureUnitIndicesBuffer);
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
			
			// We have to flush, otherwise the texture indices of previous sprites may be affected
			flush();
			
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
	
	public boolean isBlendingEnabled () {
		return !blendingDisabled;
	}

	public boolean isDrawing () {
		return drawing;
	}
}
