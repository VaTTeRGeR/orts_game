package de.vatterger.entitysystem.processors.client;

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

import de.vatterger.entitysystem.application.GameConstants;
import de.vatterger.entitysystem.components.client.InterpolatedPosition;
import de.vatterger.entitysystem.components.client.InterpolatedRotation;
import de.vatterger.entitysystem.components.shared.G3DBModelId;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.StaticModel;
import de.vatterger.entitysystem.handler.asset.ModelHandler;

@Wire
public class DrawTankInfoProcessor extends EntityProcessingSystem {

	private ComponentMapper<InterpolatedPosition>	cpm;
	private ComponentMapper<InterpolatedRotation>	crm;
	private ComponentMapper<G3DBModelId>	gmim;
	
	private Camera cam;
	private SpriteBatch spriteBatch;
	private BitmapFont bmf;
	private Matrix4 mat;
	
	private Vector3 vec1, vec2;
	
	@SuppressWarnings("unchecked")
	public DrawTankInfoProcessor(Camera cam, Environment env) {
		super(Aspect.getAspectForAll(InterpolatedPosition.class, InterpolatedRotation.class, G3DBModelId.class).exclude(Inactive.class, StaticModel.class));
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
			
			Node node = instance.getNode("hull");
			node.translation.set(cpm.get(e).getInterpolatedValue());
			node.rotation.set(Vector3.Z, crm.get(e).getInterpolatedValue());
			
			instance.calculateTransforms();
			
			vec1.set(cam.position).sub(node.globalTransform.getTranslation(vec2.setZero()));
			float rot = MathUtils.radiansToDegrees*MathUtils.atan2(vec1.y, vec1.x);
			
			mat.idt().translate(node.globalTransform.getTranslation(vec2.setZero()).add(0, 0, 8f)).scl(0.05f).rotate(Vector3.X, 90f).rotate(Vector3.Y, rot+90f).mulLeft(cam.combined);
			spriteBatch.setProjectionMatrix(mat);
			bmf.draw(spriteBatch, new GlyphLayout(bmf, "I'm a Tank!\nI'm a Taaaank!\nProblem?!\nI'm a Taaaank!", new Color(1, 0, 0, 1f-MathUtils.clamp(vec1.len()/GameConstants.TEXT_RANGE, 0f, 1f)), 0f, Align.center, false), 0, 0);
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
