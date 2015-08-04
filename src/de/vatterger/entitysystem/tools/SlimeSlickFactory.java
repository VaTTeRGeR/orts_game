package de.vatterger.entitysystem.tools;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.Vector3;
import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.RemoteMaster;
import de.vatterger.entitysystem.components.Saveable;
import de.vatterger.entitysystem.components.Velocity;

public class SlimeSlickFactory {
	
	final static float INITIAL_SIZE = 2f;
	final static float SMALL_EDIBLE_SIZE = 1f;
	
	private SlimeSlickFactory() {}
	
	public static Entity createSlime(World world, Vector3 position) {
		return world.createEntity().edit()
			.add(new Position(position))
			.add(new CircleCollision(INITIAL_SIZE))
			.add(new Velocity())
			.add(new Saveable())
			.add(new RemoteMaster(Position.class,Velocity.class,CircleCollision.class))
		.getEntity();
	}

	public static Entity createSmallEdible(World world, Vector3 position) {
		return world.createEntity().edit()
			.add(new Position(position))
			.add(new CircleCollision(SMALL_EDIBLE_SIZE))
			.add(new Saveable())
			.add(new RemoteMaster(Position.class,CircleCollision.class))
		.getEntity();
	}
}
