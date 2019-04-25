package de.vatterger.game.systems.graphics;

import java.util.Arrays;
import java.util.Comparator;

import org.lwjgl.opengl.GL11;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.SpriteScale;
import de.vatterger.game.components.gameobject.SpriteDrawMode;
import de.vatterger.game.components.gameobject.SpriteFrame;
import de.vatterger.game.components.gameobject.SpriteID;
import de.vatterger.game.components.gameobject.SpriteLayer;

public class SpriteRenderSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition>	pm;
	private ComponentMapper<AbsoluteRotation>	srm;
	private ComponentMapper<SpriteScale>		sm;
	private ComponentMapper<SpriteID>			sim;
	private ComponentMapper<SpriteLayer>		slm;
	private ComponentMapper<CullDistance>		cdm;
	private ComponentMapper<SpriteDrawMode>		sdmm;
	private ComponentMapper<SpriteFrame>		sfm;

	@Wire(name="camera")
	private Camera camera;
	
	private SpriteBatch spriteBatch;
	
	private Vector3 v0 = new Vector3();
	
	private int 		addedEntitiesSize = 0;
	
	private Integer[]	renderArray = new Integer[0];
	private int			renderArraySize = 0;
	
	private final SpriteDrawMode spriteDrawModemodeDefault = new SpriteDrawMode();
	
	//ShaderProgram program;
	
	private Profiler profiler = new Profiler("SpriteRender");
	
	@SuppressWarnings("unchecked")
	public SpriteRenderSystem() {
		
		super(Aspect.all(SpriteID.class, AbsolutePosition.class, SpriteLayer.class).exclude(Culled.class));
		
		this.spriteBatch = new SpriteBatch(8191);
		
		GraphicalProfilerSystem.registerProfiler("SpriteRender", Color.CYAN, profiler);

		/*program =  new ShaderProgram(Gdx.files.internal("assets/shader/terrain.vert"), Gdx.files.internal("assets/shader/terrain.frag"));
		if (program.isCompiled()) {
			spriteBatch.setShader(program);
		} else {
			System.out.println(program.getLog());
		}*/
	}
	
	@Override
	protected void initialize() {
		spriteBatch.enableBlending();
		addedEntitiesSize = 0;
	}
	
	@Override
	protected void inserted(int entityId) {
		addedEntitiesSize++;
	}
	
	@Override
	protected void removed(int entityId) {
		addedEntitiesSize--;
	}
	
	@Override
	protected void begin() {
		
		profiler.start();
		
		renderArraySize = 0;
		
		if(renderArray.length < addedEntitiesSize || renderArray.length > addedEntitiesSize * 4) {
			renderArray = new Integer[addedEntitiesSize*2];
		}
		
		Arrays.fill(renderArray, null);
		
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
	}
	
	@Override
	protected void process(int e) {
		if(!cdm.has(e) || cdm.get(e).visible) {
			renderArray[renderArraySize++] = Integer.valueOf(e);
		}
	}
	
	private final Comparator<Integer> yzcomp = new Comparator<Integer>() {

		@Override
		public int compare(Integer o1, Integer o2) {
			
			Vector3 v1 = pm.get(o1).position;
			Vector3 v2 = pm.get(o2).position;
			
			int sl1 = slm.get(o1).v;
			int sl2 = slm.get(o2).v;
			
			if(sl1 == sl2 && v1.y == v2.y && v1.z == v2.z){
				return 0;

			} else if(sl1 == sl2 && (v1.y != v2.y || v1.z != v2.z)) {
				return v1.y-v1.z < v2.y-v2.z  ? 1 : -1;

			} else if(sl1 != sl2){
				return sl1-sl2;
			}
			
			return 0;
		}
	};
	
	@Override
	protected void end() {
		
		Arrays.sort(renderArray, 0, renderArraySize, yzcomp);
		
		for (int r = 0; r < renderArray.length && renderArray[r] != null; r++) {
			
			final int e = renderArray[r];
			
			final Vector3 pos = pm.get(e).position;
			final SpriteID sidc = sim.get(e);
			final AbsoluteRotation ar = srm.getSafe(e, null);
			final SpriteFrame sf = sfm.getSafe(e, null);
			
			v0.set(pos);
			
			final Sprite sprite;
			
			if(ar == null) {

				if(sf == null) {
					
					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id);
					
 				} else {
 					
 					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, sf.currentframe);
 				}
				
			} else {
				
				ar.rotation = Math2D.normalize_360(ar.rotation);
				
				if(sf != null) {
 				
					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, sf.currentframe);

					sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
					sprite.setRotation(Math2D.roundAngle(ar.rotation, 16));
					
				} else if(AtlasHandler.isEightAngleSprite(sidc.id)) {

					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, Math2D.angleToIndex(ar.rotation, 8));
					
				} else if(AtlasHandler.isSixteenAngleSprite(sidc.id)) {

					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, Math2D.angleToIndex(ar.rotation, 16));
					
				} else {
					
					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id);
					
					sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
					sprite.setRotation(Math2D.roundAngle(ar.rotation,16));
				}
			}
			
			if(sm.has(e)) {
				
				sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
				sprite.setScale(sm.get(e).scale);
			}
			
			final float sx =   v0.x							 - sprite.getWidth()  * 0.5f;
			final float sy = ( v0.y + v0.z ) * Metrics.ymodp - sprite.getHeight() * 0.5f;

			sprite.setPosition(sx, sy);
			
			// We use pre-multiplied alhpa!
			// https://www.shawnhargreaves.com/blog/premultiplied-alpha.html
			SpriteDrawMode sdm = null;
			
			if(sdmm.has(e)) {
				sdm = sdmm.get(e);
			} else {
				sdm = spriteDrawModemodeDefault;
			}

			// This already gets checked in the batch for equal draw mode to avoid flushes
			spriteBatch.setBlendFunction(sdm.blend_src, sdm.blend_dst);

			if(!sdm.color.equals(Color.WHITE))
				sprite.setColor(sdm.color);

			sprite.draw(spriteBatch);
			
			if(!sdm.color.equals(Color.WHITE))
				sprite.setColor(Color.WHITE);
			
			if(ar != null)
				sprite.setRotation(0f);
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
