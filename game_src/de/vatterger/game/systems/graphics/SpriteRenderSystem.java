package de.vatterger.game.systems.graphics;

import java.util.Arrays;
import java.util.Comparator;

import org.lwjgl.opengl.GL11;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.SpriteScale;
import de.vatterger.game.components.gameobject.SpriteDrawMode;
import de.vatterger.game.components.gameobject.SpriteID;
import de.vatterger.game.components.gameobject.SpriteLayer;

public class SpriteRenderSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition>	pm;
	private ComponentMapper<AbsoluteRotation>	srm;
	private ComponentMapper<SpriteScale>				sm;
	private ComponentMapper<SpriteID>			sim;
	private ComponentMapper<SpriteLayer>		slm;
	private ComponentMapper<CullDistance>		cdm;
	private ComponentMapper<SpriteDrawMode>		sdmm;

	private SpriteBatch spriteBatch;
	
	private Camera camera;
	
	private Vector3 v0 = new Vector3();
	
	private Integer[] renderArray = new Integer[0];
	private int renderSize = 0;

	private int renderArrayPointer = 0;
	
	//ShaderProgram program;
	
	@SuppressWarnings("unchecked")
	public SpriteRenderSystem(Camera camera) {
		super(Aspect.all(SpriteID.class, AbsolutePosition.class, SpriteLayer.class).exclude(Culled.class));
		this.camera = camera;
		this.spriteBatch = new SpriteBatch();
		
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
		renderSize = 0;
	}
	
	@Override
	protected void inserted(int entityId) {
		renderSize++;
	}
	
	@Override
	protected void removed(int entityId) {
		renderSize--;
	}
	
	@Override
	protected void begin() {
		renderArrayPointer = 0;
		
		if(renderArray.length < renderSize || renderArray.length > renderSize*4) {
			renderArray = new Integer[renderSize*2];
		}
		
		Integer mo = new Integer(-1);
		Arrays.fill(renderArray, mo);
		
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
	}
	
	@Override
	protected void process(int e) {
		if(!cdm.has(e) || cdm.get(e).visible) {
			renderArray[renderArrayPointer++] = new Integer(e);
		}
	}
	
	private Comparator<Integer> yzcomp = new Comparator<Integer>() {
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
	
	float flashRot = 0f;
	
	@Override
	protected void end() {
		Arrays.sort(renderArray, 0, renderArrayPointer, yzcomp);
		for (int r = 0; r < renderArray.length && renderArray[r] != -1; r++) {
			int e = renderArray[r];
			
			Vector3 pos = pm.get(e).position;
			SpriteID sidc = sim.get(e);
			AbsoluteRotation sr = srm.getSafe(e, null);
			
			v0.set(pos);
			
			Sprite sprite;
			
			if(sr == null) {
				sprite = AtlasHandler.getSharedSpriteFromId(sidc.id);
			} else {
				sr.rotation = Math2D.normalize_360(sr.rotation);
				if(AtlasHandler.isEightAngleSprite(sidc.id)) {
					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, Math2D.angleToIndex(sr.rotation, 8));
					
				} else if(AtlasHandler.isSixteenAngleSprite(sidc.id)) {
					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, Math2D.angleToIndex(sr.rotation, 16));
					
				} else {
					sprite = AtlasHandler.getSharedSpriteFromId(sidc.id);
					sprite.setOrigin(Metrics.sssm/2f, Metrics.sssm/2f);
					sprite.setRotation(Math2D.roundAngle(sr.rotation,16));
				}
			}
			
			if(sm.has(e)) {
				sprite.setOrigin(sprite.getWidth()/2f, sprite.getHeight()/2f);
				sprite.setScale(sm.get(e).scale);
			}
			
			float sx =  v0.x							-	sprite.getWidth() /2f;
			float sy = (v0.y + v0.z) * Metrics.ymodp	-	sprite.getHeight()/2f;
			sprite.setPosition(sx, sy);
			
			if(sdmm.has(e)) {
				SpriteDrawMode sdm = sdmm.get(e);
				
				sprite.setColor(sdm.color);
				spriteBatch.setBlendFunction(sdm.blend_src, sdm.blend_dst);
				sprite.draw(spriteBatch);
				
				spriteBatch.setBlendFunction(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			} else {
				sprite.draw(spriteBatch);
			}
			sprite.setRotation(0f);
		}
		spriteBatch.end();
	}
	
	@Override
	protected void dispose() {
		spriteBatch.dispose();
		//program.dispose();
	}
}
