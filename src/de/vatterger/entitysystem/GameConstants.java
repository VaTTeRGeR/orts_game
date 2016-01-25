package de.vatterger.entitysystem;

import com.esotericsoftware.minlog.Log;

public class GameConstants {

	private GameConstants(){}

	/**The maximum x and y values that the playable area extends to from [0,0]*/
	public static final int XY_BOUNDS = 1024; // Meters

	public static final float NET_SYNC_AREA = 500; // Meters

	public static final int TANK_COUNT_INIT = 2000;

	public static final float TANK_COLLISION_RADIUS = 2f; // Meters

	public static final float TANK_VIEW_RANGE = 50f;

	public static final int GRIDMAP_CELLSIZE = 32; // Meters

	/**Draw debug lines for the net-synchronized area**/
	public static final boolean DEBUG_SYNC_AREA = false;

	/**Draw debug lines for the net-synchronized area**/
	public static final boolean DEBUG_MAP_BORDER = false;

	/**Draw debug lines for the unit selection raycast**/
	public static final boolean DEBUG_MOUSE_RAY_INTERSECTION = true;

	/**Networking debug Loglevel : NONE*/
	public static final int NET_LOGLEVEL = Log.LEVEL_NONE; // Log.LEVEL_X
	
	/**Local server IP-Address*/
	public static final String LOCAL_SERVER_IP = "localhost";

	/**Internet server IP-Address*/
	public static final String NET_SERVER_IP = "46.101.156.87";

	/**Port to bind TCP and UDP port to*/
	public static final int NET_PORT = 26000; // Port

	/**The max time to wait before an attempted connection is canceled (in ms)*/
	public static final int NET_CONNECT_TIMEOUT = 2000; // Milliseconds

	/**Output buffer size (in bytes)*/
	public static final int QUEUE_BUFFER_SIZE = 1300*64; // Bytes

	/**Object graph buffer size (in bytes)*/
	public static final int OBJECT_BUFFER_SIZE = 1300; // Bytes

	/**(Probably) Optimal packet-size to use over the internet (in bytes)*/
	public static final int PACKETSIZE_INTERNET = 1200; // Bytes
	
	/**Packet count per tick per player*/
	public static final int PACKETS_PER_TICK = 2; // 20 fps, 1200B/packet => 40 packets/s/player => 46KB/s/player
	
	/**Estimated Interpolation time*/
	public static final float INTERPOLATION_PERIOD_TARGET = 0.5f; // Seconds
	
	/**Measured Interpolation time*/
	public static float INTERPOLATION_PERIOD_MEASURED = 0.5f; // Seconds
	
	/**Interpolation Time-frame*/
	public static final float EXTRAPOLATION_FACTOR = 3f; // E_F x I_P_T = T_T_S
	
	/**After this time has elapsed without an update, the respective entity is deleted by the client*/
	public static final float ENTITY_UPDATE_TIMEOUT = 3f; // Seconds
	
	/**After this time has elapsed while an inactive-marker-component is added to an entity, the respective entity is deleted from the entity system*/
	public static final float INACTIVE_DELETION_DELAY = 3f; // Seconds
}
