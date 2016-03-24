package de.vatterger.entitysystem.factory.server;

import static de.vatterger.entitysystem.application.GameConstants.TANK_COLLISION_RADIUS;
import static de.vatterger.entitysystem.application.GameConstants.XY_BOUNDS;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.application.GameConstants;
import de.vatterger.entitysystem.components.server.RemoteMaster;
import de.vatterger.entitysystem.components.server.RemoteMasterRebuild;
import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.server.ServerRotation;
import de.vatterger.entitysystem.components.server.ServerTurretRotation;
import de.vatterger.entitysystem.components.shared.ActiveCollision;
import de.vatterger.entitysystem.components.shared.CircleCollision;
import de.vatterger.entitysystem.components.shared.G3DBModelId;
import de.vatterger.entitysystem.components.shared.GridMapFlag;
import de.vatterger.entitysystem.components.shared.VehicleProperties;
import de.vatterger.entitysystem.components.shared.Velocity;
import de.vatterger.entitysystem.components.shared.ViewRange;
import de.vatterger.entitysystem.components.shared.WaypointPath;
import de.vatterger.entitysystem.handler.asset.ModelHandler;
import de.vatterger.entitysystem.handler.gridmap.GridMapBitFlag;

public class ServerTankFactory {
	
	private ServerTankFactory() {}
	
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
			.add(new G3DBModelId(ModelHandler.getModelId("panzer_i_b")))
			.add(new ServerRotation(0f))
			.add(new ServerTurretRotation(0f))
			.add(new VehicleProperties(20f+15*2*(MathUtils.random()-0.5f),-1f))
			.add(new ViewRange(GameConstants.TANK_VIEW_RANGE))
			.add(new RemoteMaster(ServerPosition.class, ServerRotation.class, ServerTurretRotation.class, G3DBModelId.class))
			.add(new RemoteMasterRebuild())
			.add(new GridMapFlag(new GridMapBitFlag(GridMapBitFlag.COLLISION|GridMapBitFlag.NETWORKED|GridMapBitFlag.ACTIVE)))
		.getEntity();
	}
	
	private static float r() {
		return MathUtils.random(1, XY_BOUNDS-1);
	}
}
