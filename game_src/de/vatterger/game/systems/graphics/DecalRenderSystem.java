package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;

public class DecalRenderSystem extends IteratingSystem {

	private DecalBatch decalBatch;
	
	private Camera		camera;

	private ComponentMapper<Position> pm;
	private ComponentMapper<Rotation> rm;
	private ComponentMapper<CullDistance> cdm;
	
	private Vector3 flyWeightVector3 = new Vector3();

	private TextureRegion region;

	public DecalRenderSystem(Camera camera, Environment environment) {
		super(Aspect.all(Position.class, Rotation.class));
		this.camera = camera;
		
		decalBatch = new DecalBatch(1024, new CameraGroupStrategy(camera));
		region = new TextureRegion(new Texture("white_light.png"));
	}
	
	protected void process(int e) {
		flyWeightVector3.set(pm.get(e).v);
		if(!cdm.has(e) || camera.frustum.sphereInFrustum(flyWeightVector3, cdm.get(e).v)) {
			Decal decal = Decal.newDecal(region, true);
			decal.setPosition(flyWeightVector3);
			if(cdm.has(e))
				decal.setDimensions(cdm.get(e).v, cdm.get(e).v);
			else
				decal.setDimensions(1f, 1f);
			decal.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
			decal.lookAt(camera.position, Vector3.Z);
			decalBatch.add(decal);
		}
	}

	@Override
	protected void end() {
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glDepthMask(false);
		decalBatch.flush();
		Gdx.gl.glDepthMask(true);
	}
	
	@Override
	protected void dispose() {
		decalBatch.dispose();
	}
}
