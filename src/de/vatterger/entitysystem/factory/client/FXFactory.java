package de.vatterger.entitysystem.factory.client;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.client.AlphaBlend;
import de.vatterger.entitysystem.components.client.LocalPosition;
import de.vatterger.entitysystem.components.client.LocalRotation;
import de.vatterger.entitysystem.components.client.LocalVelocity;
import de.vatterger.entitysystem.components.shared.G3DBModelId;
import de.vatterger.entitysystem.components.shared.GridMapFlag;
import de.vatterger.entitysystem.components.shared.TimedDelete;
import de.vatterger.entitysystem.handler.asset.ModelHandler;
import de.vatterger.entitysystem.handler.gridmap.GridMapBitFlag;

public class FXFactory {
	
	private FXFactory() {}

	public static Entity createTracer(World world, Node node, float speed, String model){
		return FXFactory.createTracer(
			world,
			node.globalTransform.getTranslation(new Vector3()),
			node.globalTransform.getRotation(new Quaternion()).transform(new Vector3(Vector3.X)).setLength(speed),
			5f,
			model
		);
	}

	public static Entity createTracer(World world, Vector3 position, Vector3 speed, float lifeTime, String model) {
		Entity e = world.createEntity();
		return e.edit()
			.add(new LocalPosition(new Vector3(position)))
			.add(new LocalRotation(0f))
			.add(new LocalVelocity(new Vector3(speed)))
			.add(new G3DBModelId(ModelHandler.getModelId(model)))
			.add(new AlphaBlend(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE)))
			.add(new GridMapFlag(new GridMapBitFlag(GridMapBitFlag.ACTIVE)))
			.add(new TimedDelete(lifeTime))
		.getEntity();
	}
}
