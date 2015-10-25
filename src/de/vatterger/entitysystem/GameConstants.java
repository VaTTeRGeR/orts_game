package de.vatterger.entitysystem;

import com.esotericsoftware.minlog.Log;

public class GameConstants {

	private GameConstants(){}

	/**The maximum x and y values that the playable area extends to from [0,0]*/
	public final static int XY_BOUNDS = 1000;

	public final static float NET_SYNC_AREA = 2000;

	public final static int TANK_COUNT_INIT = 2000;
	
	public final static int TANK_COUNT_PER_TICK = 0;

	public final static float TANK_COLLISION_RADIUS = 3f;

	public final static int GRIDMAP_CELLSIZE = 50;

	public final static float minZoom = 0.0001f, maxZoom = 100000f;

	/**Networking Debug Loglevel : NONE*/
	public final static int NET_LOGLEVEL = Log.LEVEL_NONE;
	
	/**Local server IP-Address*/
	public static final String LOCAL_SERVER_IP = "localhost";

	/**Internet server IP-Address*/
	public static final String NET_SERVER_IP = "localhost";

	/**Port to bind TCP and UDP port to*/
	public static final int NET_PORT = 26000;

	/**The max time to wait before an attempted connection is canceled (in ms)*/
	public static final int NET_CONNECT_TIMEOUT = 5000;

	/**Output buffer size (in bytes)*/
	public static final int QUEUE_BUFFER_SIZE = 64000; // Bytes

	/**Object graph buffer size (in bytes)*/
	public static final int OBJECT_BUFFER_SIZE = 1400; // Bytes
	
	/**(Probably) Optimal packet-size to use over the internet (in bytes)*/
	public static final int PACKETSIZE_INTERNET = 1200;// Bytes
	
	/**Packet count per tick per player*/
	public static final int PACKETS_PER_TICK = 1;
	
	/**Interpolation Time-frame*/
	public static final float INTERPOLATION_PERIOD = 0.5f;
	
	/**Measured Interpolation Time-frame*/
	public static float INTERPOLATION_PERIOD_MEASURED = 0.5f;
	
	/**Interpolation Time-frame*/
	public static final float EXTRAPOLATION_FACTOR = 3f;
	
	/**After this time has elapsed without an update, the respective entity is deleted by the client*/
	public static final float ENTITY_UPDATE_TIMEOUT = 5f;// Seconds
	
	public static final float INACTIVE_DELETION_DELAY = 5f;
}
