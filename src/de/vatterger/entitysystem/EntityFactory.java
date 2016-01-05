package de.vatterger.entitysystem;

import static de.vatterger.entitysystem.GameConstants.TANK_COLLISION_RADIUS;
import static de.vatterger.entitysystem.GameConstants.XY_BOUNDS;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.entitysystem.components.client.AlphaBlend;
import de.vatterger.entitysystem.components.client.InterpolatedPosition;
import de.vatterger.entitysystem.components.client.InterpolatedRotation;
import de.vatterger.entitysystem.components.client.LocalPosition;
import de.vatterger.entitysystem.components.client.LocalRotation;
import de.vatterger.entitysystem.components.client.LocalVelocity;
import de.vatterger.entitysystem.components.server.ComponentVersioningRegister;
import de.vatterger.entitysystem.components.server.DataBucket;
import de.vatterger.entitysystem.components.server.EntityAckBucket;
import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.components.server.RemoteMaster;
import de.vatterger.entitysystem.components.server.RemoteMasterRebuild;
import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.server.ServerRotation;
import de.vatterger.entitysystem.components.server.ServerTurretRotation;
import de.vatterger.entitysystem.components.shared.ActiveCollision;
import de.vatterger.entitysystem.components.shared.CircleCollision;
import de.vatterger.entitysystem.components.shared.G3DBModelId;
import de.vatterger.entitysystem.components.shared.GridMapFlag;
import de.vatterger.entitysystem.components.shared.Name;
import de.vatterger.entitysystem.components.shared.NetPriorityQueue;
import de.vatterger.entitysystem.components.shared.NetSynchedArea;
import de.vatterger.entitysystem.components.shared.Ping;
import de.vatterger.entitysystem.components.shared.StaticModel;
import de.vatterger.entitysystem.components.shared.Velocity;
import de.vatterger.entitysystem.components.shared.ViewRange;
import de.vatterger.entitysystem.components.shared.WaypointPath;
import de.vatterger.entitysystem.handler.asset.ModelHandler;
import de.vatterger.entitysystem.handler.gridmap.GridMapBitFlag;

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
			.add(new G3DBModelId(ModelHandler.getModelId("panzeri")))
			.add(new ServerRotation(0f))
			.add(new ServerTurretRotation(0f))
			.add(new ViewRange(GameConstants.TANK_VIEW_RANGE))
			.add(new RemoteMaster(ServerPosition.class, ServerRotation.class, ServerTurretRotation.class, G3DBModelId.class))
			.add(new RemoteMasterRebuild())
			.add(new GridMapFlag(new GridMapBitFlag(GridMapBitFlag.COLLISION|GridMapBitFlag.NETWORKED|GridMapBitFlag.ACTIVE)))
		.getEntity();
	}
	
	public static Entity createTerrainTile(World world, Vector2 position) {
		Entity e = world.createEntity();
		return e.edit()
			.add(new InterpolatedPosition(new Vector3(position, 0f)))
			.add(new InterpolatedRotation(0f))
			.add(new StaticModel())
			.add(new G3DBModelId(ModelHandler.getModelId("terrain")))
		.getEntity();
	}

	private static float r() {
		return MathUtils.random(1, XY_BOUNDS-1);
	}
	
	public static Entity createRTSPlayer(World world, Connection c) {
		return world.createEntity().edit()
			.add(new KryoConnection(c))
			.add(new Ping())
			.add(new DataBucket())
			.add(new EntityAckBucket())
			.add(new ComponentVersioningRegister())
			.add(new Name("#Player "+c))
			.add(new NetSynchedArea(new Rectangle(0,0,256,256)))
			.add(new NetPriorityQueue())
		.getEntity();
	}

	public static Entity createBulletEffect(World world, Vector3 position, Vector3 speed) {
		Entity e = world.createEntity();
		return e.edit()
			.add(new LocalPosition(new Vector3(position)))
			.add(new LocalRotation(0f))
			.add(new LocalVelocity(new Vector3(speed)))
			.add(new G3DBModelId(ModelHandler.getModelId("tracer_panzeri")))
			.add(new AlphaBlend())
			.add(new GridMapFlag(new GridMapBitFlag(GridMapBitFlag.NETWORKED|GridMapBitFlag.ACTIVE)))
		.getEntity();
	}
}
