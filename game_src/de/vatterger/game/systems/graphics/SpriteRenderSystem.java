package de.vatterger.game.systems.graphics;

import java.util.function.IntBinaryOperator;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.ArrayTextureSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.IntArrayTimSort;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.CullMetersPerPixel;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.SpriteDrawMode;
import de.vatterger.game.components.gameobject.SpriteFrame;
import de.vatterger.game.components.gameobject.SpriteID;
import de.vatterger.game.components.gameobject.SpriteLayer;

public class SpriteRenderSystem extends BaseEntitySystem {

	private ComponentMapper<AbsolutePosition>		apm;
	private ComponentMapper<AbsoluteRotation>		arm;
	private ComponentMapper<SpriteID>				sidm;
	private ComponentMapper<SpriteLayer>			slm;
	private ComponentMapper<SpriteDrawMode>		sdmm;
	private ComponentMapper<SpriteFrame>			sfm;
	private ComponentMapper<CullMetersPerPixel>	cmppm;
	
	@Wire(name="camera")
	private Camera camera;
	
	@Wire(name="viewport")
	private Viewport viewport;
	
	private Batch spriteBatch;
	
	// max is 8191
	private final int numSpritesPerBatch = 2048;
	
	//private final Array<Integer> renderArray = new Array<>(false, numSpritesPerBatch, Integer.class);
	private final IntArray renderArray = new IntArray(false, 32*1024);
	
	// Enough to sort 16tsd sprites
	private final int[] renderArrayTmp = new int[32*1024];
	
	private final SpriteDrawMode spriteDrawModemodeDefault = new SpriteDrawMode();
	
	private Profiler profiler = new Profiler("SpriteRender");
	
	// The default sprite that is used if the requested one can't be found.
	private final int error_sid;
	
	public SpriteRenderSystem() {
		
		super(Aspect.all(SpriteID.class, AbsolutePosition.class, SpriteLayer.class, CullMetersPerPixel.class).exclude(Culled.class));
		
		try {
			spriteBatch = new ArrayTextureSpriteBatch(numSpritesPerBatch, 1024, 1024, 16, GL30.GL_NEAREST, GL30.GL_LINEAR_MIPMAP_LINEAR);
			
		} catch (Exception e) {
			
			System.err.println(e.getMessage());
			
			spriteBatch = new SpriteBatch(numSpritesPerBatch);
		}
		
		spriteBatch.enableBlending();
		
		GraphicalProfilerSystem.registerProfiler("SpriteRender", Color.CYAN, profiler);

		error_sid = AtlasHandler.getIdFromName("error");
	}
	
	@Override
	protected void inserted(int entityId) {
		renderArray.add(entityId);
		//System.out.println("Sprites: " + renderArray.size);
	}
	
	@Override
	protected void removed(int entityId) {
		renderArray.removeValue(entityId);
		//System.out.println("Sprites: " + renderArray.size);
	}
	
	final IntBinaryOperator yzComparator = new IntBinaryOperator() {
		
		@Override
		public int applyAsInt(int a, int b) {
			
			final int sl_diff = slm.get(a).v - slm.get(b).v;
			
			if(sl_diff == 0) {
				
				final Vector3 v1 = apm.get(a).position;
				final Vector3 v2 = apm.get(b).position;
				
				if(v1.y != v2.y || v1.z != v2.z) {
					
					float yz1 = v1.y - v1.z;
					float yz2 = v2.y - v2.z;
					
					if(yz1  < yz2) {
						
						return 1;
						
					} else if(yz1  > yz2) {
						
						return -1;
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
		
		//System.out.println("LRU: " + ((ArrayTextureSpriteBatch)spriteBatch).getTextureLFUSize() + "/" + ((ArrayTextureSpriteBatch)spriteBatch).getTextureLFUCapacity() + " - Swaps: " + ((ArrayTextureSpriteBatch)spriteBatch).getTextureLFUSwaps());
		
		spriteBatch.begin();
		
		//Profiler p_sort = new Profiler("Sorting Renderarray", TimeUnit.MICROSECONDS);
		
		final int[]	renderArrayData	= renderArray.items;
		final int	renderArraySize	= renderArray.size;
		
		IntArrayTimSort.sort(renderArrayData, 0, renderArraySize, yzComparator, renderArrayTmp, 0, renderArrayTmp.length);
		
		//p_sort.log();
		
		//Profiler p_render = new Profiler("Render", TimeUnit.MICROSECONDS);

		//Profiler p_iterate = new Profiler("Building vertices", TimeUnit.MICROSECONDS);
		
		float mpp = viewport.getWorldWidth()/viewport.getScreenWidth();
		
		for (int i = 0; i < renderArraySize; i++) {
			
			final int entityId = renderArrayData[i];
			
			// LOD check
			if(cmppm.get(entityId).mpp < mpp) {
				continue;
			}
			
			int sid = sidm.get(entityId).id;
			
			AbsoluteRotation ar = arm.getSafe(entityId, null);
			SpriteFrame sf = sfm.getSafe(entityId, null);
			
			if(sid < 0) {
				sid = error_sid;
			}
			
			final Sprite sprite;
			
			if(ar != null && ar.rotation != 0f) {
				
				ar.rotation = Math2D.normalize_360(ar.rotation);
				
				if(sf != null) {
					sprite = AtlasHandler.getSharedSpriteFromId(sid, sf.currentframe);

					sprite.setRotation(Math2D.roundAngle(ar.rotation, 16));
					
				} else if(AtlasHandler.isEightAngleSprite(sid)) {
					sprite = AtlasHandler.getSharedSpriteFromId(sid, Math2D.angleToIndex(ar.rotation, 8));
					
				} else if(AtlasHandler.isSixteenAngleSprite(sid)) {
					sprite = AtlasHandler.getSharedSpriteFromId(sid, Math2D.angleToIndex(ar.rotation, 16));
					
				} else {
					
					sprite = AtlasHandler.getSharedSpriteFromId(sid);
					
					//sprite.setRotation(Math2D.roundAngle(ar.rotation,16));
				}
				
			} else {

				if(sf != null) {
					
 					sprite = AtlasHandler.getSharedSpriteFromId(sid, sf.currentframe);
					
 				} else {
 					
					sprite = AtlasHandler.getSharedSpriteFromId(sid);
 				}
			}
			
			final Vector3 pos = apm.get(entityId).position;
			
			final float sx =   pos.x						   		- sprite.getWidth()  * 0.5f;
			final float sy = ( pos.y + pos.z ) * Metrics.ymodp - sprite.getHeight() * 0.5f;
			
			sprite.setPosition(sx, sy);
			
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
			
			final float[] vertices = sprite.getVertices();
			
			spriteBatch.draw(sprite.getTexture(), vertices, 0, vertices.length);
			
			if(sdm.color != null) {
				sprite.setColor(Color.WHITE);
			}
			
			if(ar != null && ar.rotation != 0f) {
				//sprite.setRotation(0f);
			}
		}
		
		//p_iterate.log();
		
		//Profiler p_flush = new Profiler("Final Batch flush", TimeUnit.MICROSECONDS);
		
		spriteBatch.end();
		
		//p_flush.log();
		
		//p_render.log();
		
		//System.out.println("Swaps: " + ((ArrayTextureSpriteBatch)spriteBatch).getTextureLFUSwaps());
		//System.out.println("Draw-calls: " + ((ArrayTextureSpriteBatch)spriteBatch).renderCalls);
		//System.out.println("Sprites: " + renderArraySize);
		
		profiler.stop();
	}

	@Override
	protected void dispose() {
		spriteBatch.dispose();
	}
}