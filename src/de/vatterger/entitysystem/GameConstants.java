package de.vatterger.entitysystem;

import com.esotericsoftware.minlog.Log;

public class GameConstants {

	private GameConstants(){}

	/**The maximum x and y values that the playable area extends to from [0,0]*/
	public final static int XY_BOUNDS = 2048;

	public final static float NET_SYNC_AREA = 512;

	public final static int TANK_COUNT_INIT = 5000;
	
	public final static int TANK_COUNT_PER_TICK = 0;

	public final static float TANK_COLLISION_RADIUS = 2f;

	public final static int GRIDMAP_CELLSIZE = 32;

	public final static float minZoom = 0.0001f, maxZoom = 100000f;

	/**Networking Debug Loglevel : NONE*/
	public final static int NET_LOGLEVEL = Log.LEVEL_ERROR;
	
	/**Local server IP-Address*/
	public static final String LOCAL_SERVER_IP = "localhost";

	/**Internet server IP-Address*/
	public static final String NET_SERVER_IP = "46.101.156.87";

	/**Port to bind TCP and UDP port to*/
	public static final int NET_PORT = 26000;

	/**The max time to wait before an attempted connection is canceled (in ms)*/
	public static final int NET_CONNECT_TIMEOUT = 1000;

	/**Output buffer size (in bytes)*/
	public static final int QUEUE_BUFFER_SIZE = 1024*16; // Bytes

	/**Object graph buffer size (in bytes)*/
	public static final int OBJECT_BUFFER_SIZE = 1300; // Bytes
	
	/**(Probably) Optimal packet-size to use over the internet (in bytes)*/
	public static final int PACKETSIZE_INTERNET = 1200;// Bytes
	
	/**Packet count per tick per player*/
	public static final int PACKETS_PER_TICK = 2;
	
	/**Interpolation Time-frame*/
	public static final float INTERPOLATION_PERIOD = 0.5f;
	
	/**Measured Interpolation Time-frame*/
	public static float INTERPOLATION_PERIOD_MEASURED = 0.5f;
	
	/**Interpolation Time-frame*/
	public static final float EXTRAPOLATION_FACTOR = 3f;
	
	/**After this time has elapsed without an update, the respective entity is deleted by the client*/
	public static final float ENTITY_UPDATE_TIMEOUT = 4f;// Seconds
	
	/**After this time has elapsed while an inactive component is added to an entity, the respective entity is deleted from the entity system*/
	public static final float INACTIVE_DELETION_DELAY = 4f;
}
