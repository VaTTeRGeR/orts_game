package de.vatterger.game.systems.graphics;

import java.util.Arrays;
import java.util.Comparator;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Metrics;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.SpriteID;
import de.vatterger.game.components.gameobject.SpriteLayer;
import de.vatterger.game.components.gameobject.SpriteRotation;

public class SpriteRenderSystem extends IteratingSystem {

	private SpriteBatch spriteBatch;
	
	private Camera camera;
	
	private ComponentMapper<Position> pm;
	private ComponentMapper<SpriteRotation> srm;
	private ComponentMapper<SpriteID> sim;
	private ComponentMapper<SpriteLayer> slm;
	
	private Vector3 v0 = new Vector3();
	private Rectangle r0 = new Rectangle();
	private Rectangle r1 = new Rectangle();
	
	private Integer[] renderArray = new Integer[0];
	private int renderSize = 0;

	private int renderArrayPointer = 0;
	
	public SpriteRenderSystem(Camera camera) {
		super(Aspect.all(SpriteID.class, Position.class, SpriteRotation.class, SpriteLayer.class));
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

	protected void process(int e) {
		Vector3 pos = pm.get(e).v;

		r0.setSize(camera.viewportWidth, camera.viewportHeight);
		r0.setCenter(camera.position.x, camera.position.y);
		
		r1.setSize(Metrics.sssm, Metrics.sssm / Metrics.ymod);
		r1.setCenter(pos.x, pos.y * Metrics.ymod);
		
		if(r0.overlaps(r1)) {
			renderArray[renderArrayPointer++] = new Integer(e);
		}
	}

	private Comparator<Integer> yzcomp = new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			Vector3 v1 = pm.get(o1).v;
			Vector3 v2 = pm.get(o2).v;
			int sl1 = slm.get(o1).v;
			int sl2 = slm.get(o2).v;
			
			if(sl1 == sl2 && v1.y == v2.y && v1.z == v2.z){
				return 0;
			} else if(sl1 == sl2 && v1.z == v2.z) {
				return v1.y < v2.y  ? 1 : -1;
			} else if(sl1 != sl2){
				return sl1-sl2;
			} else {
				return v1.z > v2.z ? 1 : -1;
			}
		}
	};
	
	@Override
	protected void end() {
		Arrays.sort(renderArray, 0, renderArrayPointer, yzcomp);
		for (int r = 0; r < renderArray.length && renderArray[r] != -1; r++) {
			int e = renderArray[r];
			Vector3 pos = pm.get(e).v;
			SpriteID sidc = sim.get(e);
			Sprite sprite = null;
			for (int i = 0; i < sidc.id.length; i++) {
				if (sidc.offset != null && sidc.offset[i] != null) {
					v0.set(sim.get(e).offset[i]).rotate(Vector3.Z, Math2D.roundAngleEight(srm.get(e).rotation[0]))
							.add(pos);
				} else {
					v0.set(pos);
				}
				sprite = AtlasHandler.getSharedSpriteFromId(sidc.id[i], Math2D.angleToIndex(srm.get(e).rotation[i]));
				sprite.setPosition(
						-sprite.getWidth() / 2f + v0.x,
						-sprite.getHeight() / 2f + (v0.y + v0.z) * Metrics.ymod);
				sprite.draw(spriteBatch);
			}
		}
		spriteBatch.end();
	}
	
	@Override
	protected void dispose() {
		spriteBatch.dispose();
	}
}
