package de.vatterger.techdemo.factory.client;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.techdemo.components.client.InterpolatedPosition;
import de.vatterger.techdemo.components.client.InterpolatedRotation;
import de.vatterger.techdemo.components.shared.G3DBModelId;
import de.vatterger.techdemo.components.shared.StaticModel;
import de.vatterger.techdemo.handler.asset.ModelHandler;

public class TerrainFactory {
	
	private TerrainFactory() {}
	
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
