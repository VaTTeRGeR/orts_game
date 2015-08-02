package de.vatterger.entitysystem.tools;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.KryoConnection;
import de.vatterger.entitysystem.components.Name;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.RemoteMaster;
import de.vatterger.entitysystem.components.Saveable;
import de.vatterger.entitysystem.components.Velocity;

public class SlimeSlickFactory {
	
	final static float INITIAL_SIZE = 2f;
	final static float SMALL_EDIBLE_SIZE = 1f;
	
	private SlimeSlickFactory() {}
	
	public static Entity createPlayer(World world, String name, Connection connection) {
		return world.createEntity().edit()
			.add(new KryoConnection(connection))
			.add(new Name(name))
		.getEntity();
	}
	
	public static Entity createSlime(World world, Vector3 position) {
		RemoteMaster rm = new RemoteMaster(3);
		rm.add(Position.class);
		rm.add(Velocity.class);
		rm.add(CircleCollision.class);
		
		return world.createEntity().edit()
			.add(new Position(position))
			.add(new CircleCollision(INITIAL_SIZE))
			.add(new Velocity())
			.add(new Saveable())
			.add(rm)
		.getEntity();
	}

	public static Entity createSmallEdible(World world, Vector3 position) {
		RemoteMaster rm = new RemoteMaster(2);
		rm.add(Position.class);
		rm.add(CircleCollision.class);
		return world.createEntity().edit()
			.add(new Position(position))
			.add(new CircleCollision(SMALL_EDIBLE_SIZE))
			.add(new Saveable())
			.add(rm)
		.getEntity();
	}
}
