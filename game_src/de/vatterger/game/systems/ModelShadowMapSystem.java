package de.vatterger.game.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.util.GameUtil;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.Model;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;

@SuppressWarnings("deprecation")
public class ModelShadowMapSystem extends IteratingSystem {

	private ComponentMapper<Position>		pm;
	private ComponentMapper<Rotation>		rm;
	private ComponentMapper<Model>			mm;
	private ComponentMapper<CullDistance>	cdm;
	
	private ModelBatch shadowModelBatch;
	private DirectionalShadowLight shadowLight;
	private Camera camera;
	private Environment environment;
	
	private Vector3 flyWeightVector3 = new Vector3();
	
	public ModelShadowMapSystem(Camera camera, Environment environment) {
		super(Aspect.all(Position.class, Model.class, Rotation.class, CullDistance.class));
		this.camera = camera;
		this.environment = environment;
	}
	
	@Override
	protected void initialize() {
		shadowLight = new DirectionalShadowLight(1024, 1024, 256f, 256f, 4f, 4096f);
		shadowLight.set(new Color(Color.BLACK), 1f, 1f, -1f);

		environment.shadowMap = shadowLight;

		shadowModelBatch = new ModelBatch(new DepthShaderProvider());
	}
	
	@Override
	protected void begin() {
		Vector3 dir = shadowLight.direction.nor();
		
		Vector3 groundIntersection = GameUtil.intersectMouseGroundPlane(camera, Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f, 0);
		float distGround = groundIntersection.dst(camera.position);

		
		Vector3 upperRight = GameUtil.intersectMouseGroundPlane(camera, Gdx.graphics.getWidth(), 0f);
		Vector3 upperLeft = GameUtil.intersectMouseGroundPlane(camera, 0f, 0f);

		float distScreenEdges = upperRight.dst(upperLeft);

		shadowLight.getCamera().viewportHeight = distScreenEdges;
		shadowLight.getCamera().viewportWidth = distScreenEdges;

		flyWeightVector3.set(dir).scl(-1f).scl(distGround).add(groundIntersection);
		
		shadowLight.begin(flyWeightVector3, dir);
		shadowModelBatch.begin(shadowLight.getCamera());
	}

	protected void process(int e) {
		if(camera.frustum.sphereInFrustum(flyWeightVector3.set(pm.get(e).v), cdm.get(e).v)) {
			ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);

			Node node = instance.nodes.first();
			node.translation.set(flyWeightVector3);
			node.rotation.set(rm.get(e).v);
		
			instance.calculateTransforms();
		
			shadowModelBatch.render(instance.getRenderable(new Renderable(), node));
		}
	}
	
	@Override
	protected void end() {
		shadowModelBatch.end();
		shadowLight.end();
	}
}
