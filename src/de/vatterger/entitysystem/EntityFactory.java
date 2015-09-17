package de.vatterger.entitysystem;

import static de.vatterger.entitysystem.util.GameConstants.*;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.entitysystem.components.ActiveCollision;
import de.vatterger.entitysystem.components.DataBucket;
import de.vatterger.entitysystem.components.Flag;
import de.vatterger.entitysystem.components.Name;
import de.vatterger.entitysystem.components.PassiveCollision;
import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.ClientConnection;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.RemoteMaster;
import de.vatterger.entitysystem.components.RemoteMasterInvalidated;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.Velocity;
import de.vatterger.entitysystem.components.ViewFrustum;
import de.vatterger.entitysystem.gridmapservice.GridFlag;

public class EntityFactory {
	
	private EntityFactory() {}
	
	public static Entity createSlime(World world, Vector2 position) {
		Entity e = world.createEntity();
		return e.edit()
			.add(new Position(new Vector3(position, 0)))
			.add(new Velocity(new Vector3(MathUtils.random(-10f, 10f), MathUtils.random(-10f, 10f),0f)))
			.add(new CircleCollision(SLIME_INITIAL_SIZE, e))
			.add(new ActiveCollision())
			.add(new RemoteMaster(Position.class, CircleCollision.class))
			.add(new RemoteMasterInvalidated())
			.add(new Flag(new GridFlag(GridFlag.COLLISION|GridFlag.NETWORKED|GridFlag.ACTIVE)))
		.getEntity();
	}

	public static Entity createSmallEdible(World world, Vector2 position) {
		Entity e = world.createEntity();
		return e.edit()
			.add(new Position(new Vector3(position, 0)))
			.add(new CircleCollision(SMALL_EDIBLE_SIZE, e))
			.add(new PassiveCollision())
			.add(new RemoteMaster(Position.class, CircleCollision.class))
			.add(new RemoteMasterInvalidated())
			.add(new Flag(new GridFlag(GridFlag.COLLISION|GridFlag.NETWORKED|GridFlag.STATIC|GridFlag.ACTIVE)))
		.getEntity();
	}
	
	public static void deactivateEntity(Entity e) {
		Flag flag = e.getComponent(Flag.class);
		if(flag != null) {
			flag.flag.removeFlag(GridFlag.ACTIVE);
		}
		e.edit().add(new Inactive());
	}
	
	public static Entity createPlayer(World world, Connection c) {
		return world.createEntity().edit()
			.add(new ClientConnection(c))
			.add(new DataBucket())
			.add(new Name("#Player "+c))
			.add(new ViewFrustum(new Rectangle()))
		.getEntity();
	}
}
