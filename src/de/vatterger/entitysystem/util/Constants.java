package de.vatterger.entitysystem.util;

public class Constants {
	
	private Constants(){}

	/**The maximum x and y values that the playable area extends to from [0,0]*/
	public final static int XY_BOUNDS = 10000;

	public final static int SLIME_ENTITYCOUNT = 100;
	
	public final static int EDIBLE_ENTITYCOUNT = 3000;

	public final static int EDIBLE_CREATE_PER_TICK = 0;

	public final static int EXPECTED_ENTITYCOUNT = EDIBLE_ENTITYCOUNT+SLIME_ENTITYCOUNT;

	public final static float SLIME_INITIAL_SIZE = 2f;

	public final static float SMALL_EDIBLE_SIZE = 0.5f;

	public final static float minZoom = 0.0001f, maxZoom = 100000f;

	/**Local server IP-Address*/
	public static final String LOCAL_SERVER_IP = "127.0.0.1";

	/**Output buffer size in bytes*/
	public static final int QUEUE_BUFFER_SIZE = 2048; // Bytes

	/**Optimal packet-size to use over the internet*/
	public static final int PACKETSIZE_INTERNET = 500;// Bytes
	
	/**Object graph buffer size plus headroom in bytes*/
	public static final int OBJECT_BUFFER_SIZE = (int)(PACKETSIZE_INTERNET*1.1f+0.5f); // Bytes
	
	/**Port to bind TCP and UDP*/
	public static final int NET_PORT = 26000;
}
