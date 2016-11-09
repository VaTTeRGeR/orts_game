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
import de.vatterger.game.components.gameobject.SpriteDrawMode;
import de.vatterger.game.components.gameobject.SpriteID;
import de.vatterger.game.components.gameobject.SpriteLayer;

public class SpriteRenderSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition> pm;
	private ComponentMapper<AbsoluteRotation> srm;
	private ComponentMapper<SpriteID> sim;
	private ComponentMapper<SpriteLayer> slm;
	private ComponentMapper<CullDistance> cdm;
	private ComponentMapper<SpriteDrawMode> sdmm;

	private SpriteBatch spriteBatch;
	
	private Camera camera;
	
	private Vector3 v0 = new Vector3();
	
	private Integer[] renderArray = new Integer[0];
	private int renderSize = 0;

	private int renderArrayPointer = 0;
	
	@SuppressWarnings("unchecked")
	public SpriteRenderSystem(Camera camera) {
		super(Aspect.all(SpriteID.class, AbsolutePosition.class, SpriteLayer.class).exclude(Culled.class));
		this.camera = camera;
		this.spriteBatch = new SpriteBatch();
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
		
		if(renderArray.length < renderSize || renderArray.length > renderSize*4)
			renderArray = new Integer[renderSize*2];
			
		Integer mo = new Integer(-1);
		Arrays.fill(renderArray, mo);
		
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
	}

	@Override
	protected void process(int e) {
		if(!cdm.has(e) || cdm.get(e).visible)
			renderArray[renderArrayPointer++] = new Integer(e);
	}

	private Comparator<Integer> yzcomp = new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			final Vector3 v1 = pm.get(o1).position;
			final Vector3 v2 = pm.get(o2).position;
			
			final int sl1 = slm.get(o1).v;
			final int sl2 = slm.get(o2).v;
			
			if(sl1 == sl2 && v1.y == v2.y){
				return 0;
			} else if(sl1 == sl2 && v1.y != v2.y) {
				return v1.y < v2.y  ? 1 : -1;
			} else if(sl1 != sl2){
				return sl1-sl2;
			} else {
				return 0;
			}
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
			} else if(AtlasHandler.isEightAngleSprite(sidc.id)) {
				sr.rotation = sr.rotation%360f;
				if(sr.rotation < 0)
					sr.rotation += 360f;
				sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, Math2D.angleToIndex(sr.rotation, 8));
			} else if(AtlasHandler.isSixteenAngleSprite(sidc.id)) {
				sr.rotation = sr.rotation%360f;
				if(sr.rotation < 0)
					sr.rotation += 360f;
				sprite = AtlasHandler.getSharedSpriteFromId(sidc.id, Math2D.angleToIndex(sr.rotation, 16));
			} else {
				sprite = AtlasHandler.getSharedSpriteFromId(sidc.id);
				sprite.setOrigin(Metrics.sssm/2f, Metrics.sssm/2f);
				sprite.setRotation(sr.rotation);
			}
			
			
			sprite.setPosition(-sprite.getWidth() / 2f + v0.x, -sprite.getHeight() / 2f + (v0.y + v0.z) * Metrics.ymodp);
			
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
	}
}
