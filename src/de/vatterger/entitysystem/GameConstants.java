package de.vatterger.entitysystem;

import com.esotericsoftware.minlog.Log;

public class GameConstants {

	private GameConstants(){}

	/**The maximum x and y values that the playable area extends to from [0,0]*/
	public final static int XY_BOUNDS = 1000;

	public final static float NET_SYNC_AREA = 256;

	public final static int SLIME_ENTITYCOUNT = 1000;
	
	public final static int EDIBLE_ENTITYCOUNT = 0;

	public final static int EDIBLE_CREATE_PER_TICK = 0;

	public final static float TANK_COLLISION_RADIUS = 3f;

	public final static int GRIDMAP_CELLSIZE = 10;

	public final static float minZoom = 0.0001f, maxZoom = 100000f;

	public final static int NET_LOGLEVEL = Log.LEVEL_NONE;
	
	/**Local server IP-Address*/
	public static final String LOCAL_SERVER_IP = "localhost";

	/**Internet server IP-Address*/
	public static final String NET_SERVER_IP = null;

	/**Output buffer size in bytes*/
	public static final int QUEUE_BUFFER_SIZE = 64000; // Bytes

	/**Object graph buffer size in bytes*/
	public static final int OBJECT_BUFFER_SIZE = 1400; // Bytes
	
	/**(Probably) Optimal packet-size to use over the internet*/
	public static final int PACKETSIZE_INTERNET = 1200;// Bytes
	
	/**Port to bind TCP and UDP*/
	public static final int NET_PORT = 26000;

	/**Packet count per tick per player*/
	public static final int PACKETS_PER_TICK = 1;
	
	/**Interpolation Time-frame*/
	public static final float INTERPOLATION_PERIOD = 0.1f;
	
	/**Measured Interpolation Time-frame*/
	public static float INTERPOLATION_PERIOD_MEASURED = 0.1f;
	
	/**Interpolation Time-frame*/
	public static final float EXTRAPOLATION_FACTOR = 2f;
	
	/**After this time has elapsed without an update, the respective entity is deleted by the client*/
	public static final float ENTITY_UPDATE_TIMEOUT = 5f;// Seconds
	
	public static final float INACTIVE_DELETION_DELAY = 5f;
}
