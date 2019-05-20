package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.engine.util.UnsafeUtil;
import de.vatterger.game.components.gameobject.*;
import sun.misc.Unsafe;

import java.util.Arrays;
import java.util.Comparator;

import static com.badlogic.gdx.graphics.g2d.Batch.*;

public class SpriteRenderSystem extends BaseEntitySystem {

	private ComponentMapper<AbsolutePosition>	apm;
	private ComponentMapper<AbsoluteRotation>	arm;
	private ComponentMapper<SpriteID>			sidm;
	private ComponentMapper<SpriteLayer>		slm;
	private ComponentMapper<SpriteDrawMode>		sdmm;
	private ComponentMapper<SpriteFrame>		sfm;

	@Wire(name="camera")
	private Camera camera;
	
	private SpriteBatch spriteBatch;
	
	private Array<Integer> renderArray = new Array<>(false, 8191, Integer.class);
	
	private final SpriteDrawMode spriteDrawModemodeDefault = new SpriteDrawMode();
	
	private float[] verticesBuffer = new float[20];
	
	private static Unsafe unsafe = UnsafeUtil.getUnsafe();
	
	private static final long FLOAT_ARRAY_OFFSET = unsafe.arrayBaseOffset(float[].class);
	
	//ShaderProgram program;
	
	private Profiler profiler = new Profiler("SpriteRender");
	
	@SuppressWarnings("unchecked")
	public SpriteRenderSystem() {
		
		super(Aspect.all(SpriteID.class, AbsolutePosition.class, SpriteLayer.class).exclude(Culled.class));
		
		// max is 8191
		spriteBatch = new SpriteBatch(1024*6);
		spriteBatch.enableBlending();
		
		GraphicalProfilerSystem.registerProfiler("SpriteRender", Color.CYAN, profiler);

		/*program =  new ShaderProgram(Gdx.files.internal("assets/shader/terrain.vert"), Gdx.files.internal("assets/shader/terrain.frag"));
		if (program.isCompiled()) {
			spriteBatch.setShader(program);
		} else {
			System.out.println(program.getLog());
		}*/
	}
	
	@Override
	protected void inserted(int entityId) {
		renderArray.add(entityId);
	}
	
	@Override
	protected void removed(int entityId) {
		renderArray.removeValue(entityId, false);
	}
	
	private final Comparator<Integer> yzcomp = new Comparator<Integer>() {
		
		@Override
		public int compare(Integer o1, Integer o2) {
			
			final int sl_diff = slm.get(o1).v - slm.get(o2).v;
			
			if(sl_diff == 0) {
				
				final Vector3 v1 = apm.get(o1).position;
				final Vector3 v2 = apm.get(o2).position;
				
				if(v1.y != v2.y || v1.z != v2.z){
					
					float yz1 = v1.y - v1.z;
					float yz2 = v2.y - v2.z;
					
					if(yz1  > yz2) {
						
						return -1;
						
					} else if(yz1  < yz2) {
						
						return 1;
					}
					
					return 0;
					
				} else {

					return 0;
				}
				
			} else {
				return sl_diff;
			}
		}
	};

	@Override
	protected void processSystem() {
		
		profiler.start();
		
		spriteBatch.setProjectionMatrix(camera.combined);
		
		spriteBatch.begin();
		
		
		final Integer[] renderArrayContent = renderArray.items;
		
		Arrays.sort(renderArrayContent, 0, renderArray.size, yzcomp);
		
		//System.out.println(renderArray.size);
		
		for (int i = 0; i < renderArray.size; i++) {
			
			final int entityId = renderArrayContent[i];
			
			final SpriteID sidc = sidm.get(entityId);
			final AbsoluteRotation ar = arm.getSafe(entityId, null);
			final SpriteFrame sf = sfm.getSafe(entityId, null);
			
			final Sprite sprite;
			
			if(ar != null && ar.rotation != 0f) {
				
				ar.rotation = Math2D.normalize_360(ar.rotation);
				
				if(sf != null) {
					
					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, sf.currentframe);

					sprite.setRotation(Math2D.roundAngle(ar.rotation, 16));
					
				} else if(AtlasHandler.isEightAngleSprite(sidc.id)) {
					
					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, Math2D.angleToIndex(ar.rotation, 8));
					
				} else if(AtlasHandler.isSixteenAngleSprite(sidc.id)) {
					
					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, Math2D.angleToIndex(ar.rotation, 16));
					
				} else {
					
					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id);

					sprite.setRotation(Math2D.roundAngle(ar.rotation,16));
				}
				
			} else {

				if(sf != null) {
					
 					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, sf.currentframe);
					
 				} else {
 					
					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id);
 				}
			}
			
			final Vector3 pos = apm.get(entityId).position;
			
			final float sx =   pos.x						   - sprite.getWidth()  / 2;
			final float sy = ( pos.y + pos.z ) * Metrics.ymodp - sprite.getHeight() / 2;
			
			//sprite.setPosition(Math2D.round(sx, Metrics.ppm), Math2D.round(sy, Metrics.ppm));
			
			final SpriteDrawMode sdm;
			
			if(sdmm.has(entityId)) {
				sdm = sdmm.get(entityId);
			} else {
				sdm = spriteDrawModemodeDefault;
			}
			
			// This already gets checked in the batch for equal draw mode to avoid flushes
			spriteBatch.setBlendFunction(sdm.blend_src, sdm.blend_dst);

			if(sdm.color != null) {
				sprite.setColor(sdm.color);
			}
			
			float[] vertices = sprite.getVertices();
			
			System.arraycopy(vertices, 0, verticesBuffer, 0, 20);
			
			verticesBuffer[X1] += sx;
			verticesBuffer[Y1] += sy;

			verticesBuffer[X2] += sx;
			verticesBuffer[Y2] += sy;

			verticesBuffer[X3] += sx;
			verticesBuffer[Y3] += sy;

			verticesBuffer[X4] += sx;
			verticesBuffer[Y4] += sy;
			
			spriteBatch.draw(sprite.getTexture(), verticesBuffer, 0, verticesBuffer.length);
			
			if(sdm.color != null) {
				sprite.setColor(Color.WHITE);
			}
			
			if(ar != null && ar.rotation != 0f) {
				sprite.setRotation(0f);
			}
		}
		
		spriteBatch.end();
		
		//System.out.println("Sprites: " + spriteBatch.maxSpritesInBatch  + "  Draw-calls: " + spriteBatch.renderCalls);
		
		profiler.stop();
	}

	@Override
	protected void dispose() {
		spriteBatch.dispose();
		//program.dispose();
	}
}