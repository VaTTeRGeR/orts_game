package de.vatterger.entitysystem.factory.client;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.client.InterpolatedPosition;
import de.vatterger.entitysystem.components.client.InterpolatedRotation;
import de.vatterger.entitysystem.components.shared.G3DBModelId;
import de.vatterger.entitysystem.components.shared.StaticModel;
import de.vatterger.entitysystem.handler.asset.ModelHandler;

public class ClientEntityFactory {
	
	private ClientEntityFactory() {}
	
	public static Entity createTerrainTile(World world, Vector2 position) {
		Entity e = world.createEntity();
		return e.edit()
			.add(new InterpolatedPosition(new Vector3(position, 0f)))
			.add(new InterpolatedRotation(0f))
			.add(new StaticModel())
			.add(new G3DBModelId(ModelHandler.getModelId("terrain")))
		.getEntity();
	}
}
