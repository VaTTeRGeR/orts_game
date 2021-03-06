package de.vatterger.techdemo.processors.client;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.techdemo.application.GameConstants;
import de.vatterger.techdemo.components.client.InterpolatedPosition;
import de.vatterger.techdemo.components.client.InterpolatedRotation;
import de.vatterger.techdemo.components.client.RemoteSlave;
import de.vatterger.techdemo.components.shared.G3DBModelId;
import de.vatterger.techdemo.components.shared.Inactive;
import de.vatterger.techdemo.components.shared.StaticModel;

@Wire
public class DrawModelInfoProcessor extends EntityProcessingSystem {

	private ComponentMapper<InterpolatedPosition>	cpm;
	private ComponentMapper<InterpolatedRotation>	crm;
	private ComponentMapper<G3DBModelId>	gmim;
	private ComponentMapper<RemoteSlave>	rsm;
	
	private Camera cam;
	private SpriteBatch spriteBatch;
	private BitmapFont bmf;
	private Matrix4 mat;
	
	private Vector3 vec1, vec2;
	
	@SuppressWarnings("unchecked")
	public DrawModelInfoProcessor(Camera cam, Environment env) {
		super(Aspect.all(InterpolatedPosition.class, InterpolatedRotation.class, G3DBModelId.class).exclude(Inactive.class, StaticModel.class));
		this.cam = cam;
		spriteBatch = new SpriteBatch();
		bmf = new BitmapFont();
		bmf.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		mat = new Matrix4();
		vec1 = new Vector3();
		vec2 = new Vector3();
	}

	@Override
	protected void begin() {
		spriteBatch.begin();
	}
	
	protected void process(Entity e) {
		if (cam.position.dst(cpm.get(e).getInterpolatedValue()) < GameConstants.TEXT_RANGE) {
			ModelInstance instance = ModelHandler.getSharedInstanceByID(gmim.get(e).id);
			
			Node node = instance.getNode("a");
			node.translation.set(cpm.get(e).getInterpolatedValue());
			node.rotation.set(Vector3.Z, crm.get(e).getInterpolatedValue());
			
			instance.calculateTransforms();
			
			vec1.set(cam.position).sub(node.globalTransform.getTranslation(vec2.setZero()));
			float rotZ = MathUtils.radiansToDegrees*MathUtils.atan2(vec1.y, vec1.x);
			
			mat.idt().translate(cpm.get(e).getInterpolatedValue().add(0, 0, 0f)).scl(0.1f).rotate(Vector3.X, 90f).rotate(Vector3.Y, rotZ+90f).mulLeft(cam.combined);
			spriteBatch.setProjectionMatrix(mat);
			bmf.draw(spriteBatch, new GlyphLayout(bmf, ""+rsm.get(e).masterId, new Color(1, 0, 0, 1f-MathUtils.clamp(vec1.len()/GameConstants.TEXT_RANGE, 0f, 1f)), 0f, Align.center, false), 0, 0);
		}
	}
	
	@Override
	protected void end() {
		spriteBatch.end();
	}
	
	@Override
	protected void dispose() {
		spriteBatch.dispose();
	}
}
