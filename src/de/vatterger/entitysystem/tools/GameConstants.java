package de.vatterger.entitysystem.tools;

public class GameConstants {
	
	private GameConstants(){}

	/**The maxiumum x and y values that the playable area extends to from [0,0]*/
	public final static int XY_BOUNDS = 10000;

	public final static int SLIME_ENTITYCOUNT = 10000;
	
	public final static int EDIBLE_ENTITYCOUNT = 10000;

	public final static int EDIBLE_CREATE_PER_TICK = 1;

	public final static int EXPECTED_ENTITYCOUNT = EDIBLE_ENTITYCOUNT+SLIME_ENTITYCOUNT;

	public final static float SLIME_INITIAL_SIZE = 1f;

	public final static float SMALL_EDIBLE_SIZE = 0.5f;

	public final static float minZoom = 0.0001f, maxZoom = 100000f;

	/**Output buffer size in bytes*/
	public static final int QUEUE_BUFFER_SIZE = 1024*1024*2; // 32M
	/**Object graph buffer size in bytes*/
	public static final int OBJECT_BUFFER_SIZE = 1500; // 2K
	/**Port to bind TCP and UDP*/
	public static final int NET_PORT = 26000;
}
