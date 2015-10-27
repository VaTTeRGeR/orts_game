package de.vatterger.entitysystem;

import static de.vatterger.entitysystem.GameConstants.*;


import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.entitysystem.components.server.DataBucket;
import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.components.server.RemoteMaster;
import de.vatterger.entitysystem.components.server.RemoteMasterRebuild;
import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.server.ServerRotation;
import de.vatterger.entitysystem.components.shared.ActiveCollision;
import de.vatterger.entitysystem.components.shared.CircleCollision;
import de.vatterger.entitysystem.components.shared.Flag;
import de.vatterger.entitysystem.components.shared.G3DBModelId;
import de.vatterger.entitysystem.components.shared.Name;
import de.vatterger.entitysystem.components.shared.Velocity;
import de.vatterger.entitysystem.components.shared.ViewFrustum;
import de.vatterger.entitysystem.components.shared.WaypointPath;
import de.vatterger.entitysystem.gridmapservice.BitFlag;
import de.vatterger.entitysystem.modelregister.ModelRegister;

public class EntityFactory {
	
	private EntityFactory() {}
	
	public static Entity createTank(World world, Vector2 position) {
		Entity e = world.createEntity();
		int numPathPoints = 10;
		Vector3[] path = new Vector3[numPathPoints];
		for (int i = 0; i < path.length; i++) {
			path[i] = new Vector3(r(), r(), 0f);
		}
		return e.edit()
			.add(new ServerPosition(new Vector3(position.x, position.y, 0f)))
			.add(new WaypointPath(path, true))
			.add(new Velocity())
			.add(new CircleCollision(TANK_COLLISION_RADIUS, e))
			.add(new ActiveCollision())
			.add(new G3DBModelId(ModelRegister.getModelId("panzeri")))
			.add(new ServerRotation(0f))
			.add(new RemoteMaster(ServerPosition.class, ServerRotation.class, G3DBModelId.class))
			.add(new RemoteMasterRebuild())
			.add(new Flag(new BitFlag(BitFlag.COLLISION|BitFlag.NETWORKED|BitFlag.ACTIVE)))
		.getEntity();
	}
	
	private static float r() {
		return MathUtils.random(10f, XY_BOUNDS-10);
	}

	public static Entity createPlayer(World world, Connection c) {
		return world.createEntity().edit()
			.add(new KryoConnection(c))
			.add(new DataBucket())
			.add(new Name("#Player "+c))
			.add(new ViewFrustum(new Rectangle()))
		.getEntity();
	}

	public static Entity createBulletEffect(World world, Vector3 position, Vector3 speed) {
		Entity e = world.createEntity();
		return e.edit()
			.add(new ServerPosition(new Vector3(position)))
			.add(new Velocity(new Vector3(speed)))
			.add(new ServerRotation(0f))
			.add(new G3DBModelId(ModelRegister.DEFAULT_ID))
			.add(new Flag(new BitFlag(BitFlag.ACTIVE)))
		.getEntity();
	}
}
