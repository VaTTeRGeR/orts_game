package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.asset.ModelHandler;
import de.vatterger.engine.util.GameUtil;
import de.vatterger.engine.util.NodeRotationUtil;
import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.ModelID;
import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Rotation;
import de.vatterger.game.components.gameobject.ShadowedModel;

@SuppressWarnings("deprecation")
public class ModelShadowMapSystem extends IteratingSystem {

	private ComponentMapper<Position>		pm;
	private ComponentMapper<Rotation>		rm;
	private ComponentMapper<ModelID>			mm;
	private ComponentMapper<CullDistance>	cdm;
	
	private ModelBatch shadowModelBatch;
	private DirectionalShadowLight shadowLight;
	private Camera camera;
	private Environment environment;
	
	private Vector3 flyWeightVector3 = new Vector3();

	private final float MAX_DISTANCE_TO_CAM = 1024f;
	private final int SHADOWMAP_RESOLUTION = 4096;
	
	public ModelShadowMapSystem(Camera camera, Environment environment) {
		super(Aspect.all(Position.class, ModelID.class, Rotation.class, ShadowedModel.class));
		this.camera = camera;
		this.environment = environment;
	}
	
	@Override
	protected void initialize() {
		shadowLight = new DirectionalShadowLight(SHADOWMAP_RESOLUTION, SHADOWMAP_RESOLUTION, 256f, 256f, 4f, 4096f);
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
		Vector3 lowerLeft = GameUtil.intersectMouseGroundPlane(camera, 0f, Gdx.graphics.getHeight());

		float distScreenEdges = Math.max(upperRight.dst(upperLeft), upperRight.dst(lowerLeft));
		distScreenEdges = Math.max(distScreenEdges, upperLeft.dst(lowerLeft));

		shadowLight.getCamera().viewportHeight = distScreenEdges * 1.1f;
		shadowLight.getCamera().viewportWidth = distScreenEdges * 1.1f;

		flyWeightVector3.set(dir).scl(-1f).scl(distGround).add(groundIntersection);
		
		shadowLight.begin(flyWeightVector3, dir);
		shadowModelBatch.begin(shadowLight.getCamera());
	}

	protected void process(int e) {
		flyWeightVector3.set(pm.get(e).v);
		if(flyWeightVector3.dst(camera.position) < MAX_DISTANCE_TO_CAM && (!cdm.has(e) || camera.frustum.sphereInFrustum(flyWeightVector3, cdm.get(e).v))) {
			ModelInstance instance = ModelHandler.getSharedInstanceByID(mm.get(e).id);

			Node node = instance.nodes.first();
			node.translation.set(flyWeightVector3);
			NodeRotationUtil.setRotationByName(instance, rm.get(e));
		
			instance.calculateTransforms();
		
			shadowModelBatch.render(instance);
		}
	}
	
	@Override
	protected void end() {
		shadowModelBatch.end();
		shadowLight.end();
	}

	@Override
	protected void dispose() {
		shadowLight.dispose();
	}
}
