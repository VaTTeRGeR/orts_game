package de.vatterger.game.systems.graphics;

import java.util.function.IntBinaryOperator;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureArraySpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.IntArrayTimSort;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.SpriteDrawMode;
import de.vatterger.game.components.gameobject.SpriteFrame;
import de.vatterger.game.components.gameobject.SpriteID;
import de.vatterger.game.components.gameobject.SpriteLayer;

public class SpriteRenderSystem extends BaseEntitySystem {

	private ComponentMapper<AbsolutePosition>	apm;
	private ComponentMapper<AbsoluteRotation>	arm;
	private ComponentMapper<SpriteID>			sidm;
	private ComponentMapper<SpriteLayer>		slm;
	private ComponentMapper<SpriteDrawMode>		sdmm;
	private ComponentMapper<SpriteFrame>		sfm;

	@Wire(name="camera")
	private Camera camera;
	
	private final TextureArraySpriteBatch spriteBatch;
	
	private final int numSpritesPerBatch = 2048;
	
	//private final Array<Integer> renderArray = new Array<>(false, numSpritesPerBatch, Integer.class);
	private final IntArray renderArray = new IntArray(false, 1024*32);
	
	// Enough to sort 16tsd sprites
	private final int[] renderArrayTmp = new int[32*1024];
	
	private final SpriteDrawMode spriteDrawModemodeDefault = new SpriteDrawMode();
	
	//ShaderProgram program;
	
	private Profiler profiler = new Profiler("SpriteRender");
	
	private int error_sid;
	
	@SuppressWarnings("unchecked")
	public SpriteRenderSystem() {
		
		super(Aspect.all(SpriteID.class, AbsolutePosition.class, SpriteLayer.class).exclude(Culled.class));
		
		// max is 8191
		spriteBatch = new TextureArraySpriteBatch(numSpritesPerBatch);
		spriteBatch.enableBlending();
		
		GraphicalProfilerSystem.registerProfiler("SpriteRender", Color.CYAN, profiler);

		error_sid = AtlasHandler.getIdFromName("error");
		
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
		//System.out.println("Sprites: " + renderArray.size);
	}
	
	@Override
	protected void removed(int entityId) {
		renderArray.removeValue(entityId);
		//System.out.println("Sprites: " + renderArray.size);
	}
	
	/*private final Comparator<Integer> yzcomp = new Comparator<Integer>() {
		
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
	};*/
	
	IntBinaryOperator yzComparator = new IntBinaryOperator() {
		
		@Override
		public int applyAsInt(int a, int b) {
			
			final int sl_diff = slm.get(a).v - slm.get(b).v;
			
			if(sl_diff == 0) {
				
				final Vector3 v1 = apm.get(a).position;
				final Vector3 v2 = apm.get(b).position;
				
				if(v1.y != v2.y || v1.z != v2.z){
					
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
		
		System.out.println("LRU: " + spriteBatch.getTextureLFUSize() + "/" + spriteBatch.getTextureLFUCapacity() + " - Swaps: " + spriteBatch.getTextureLFUSwaps());
		
		spriteBatch.begin();
		
		
		
		final int[] renderArrayContent = renderArray.items;
		
		IntArrayTimSort.sort(renderArrayContent, 0, renderArray.size, yzComparator, renderArrayTmp, 0, renderArrayTmp.length);
		
		for (int i = 0; i < renderArray.size; i++) {
			
			int entityId = renderArrayContent[i];
			
			int sid = sidm.get(entityId).id;
			
			AbsoluteRotation ar = arm.getSafe(entityId, null);
			SpriteFrame sf = sfm.getSafe(entityId, null);

			final Sprite sprite;
			
			if(sid < 0) {
				sid = error_sid;
			}
			
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
					
					sprite.setRotation(Math2D.roundAngle(ar.rotation,16));
				}
				
			} else {

				if(sf != null) {
					
 					sprite = AtlasHandler.getSharedSpriteFromId(sid, sf.currentframe);
					
 				} else {
 					
					sprite = AtlasHandler.getSharedSpriteFromId(sid);
 				}
			}
			
			final Vector3 pos = apm.get(entityId).position;
			
			final float sx =   pos.x						   - sprite.getWidth()  / 2;
			final float sy = ( pos.y + pos.z ) * Metrics.ymodp - sprite.getHeight() / 2;
			
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
			
			// Copy, then translate, the sprite-vertice data to prevent floating point drift from continuous translations.
			float[] vertices = sprite.getVertices();
			
			spriteBatch.draw(sprite.getTexture(), vertices, 0, vertices.length);
			
			if(sdm.color != null) {
				sprite.setColor(Color.WHITE);
			}
			
			if(ar != null && ar.rotation != 0f) {
				sprite.setRotation(0f);
			}
		}
		
		//Profiler pSendToGPU = new Profiler("Sprite GPU upload", TimeUnit.MICROSECONDS);
		
		spriteBatch.end();
		
		//System.out.println("Render calls: " + spriteBatch.renderCalls);
		
		//pSendToGPU.log();
		
		//System.out.println("Sprites: " + spriteBatch.maxSpritesInBatch  + "  Draw-calls: " + spriteBatch.renderCalls);
		
		profiler.stop();
	}

	@Override
	protected void dispose() {
		spriteBatch.dispose();
		//program.dispose();
	}
}