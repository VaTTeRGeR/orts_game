package de.vatterger.game.systems.gameplay;

import com.artemis.BaseSystem;

/**
 * Keeps track of the elpased time and provides static access to often used time values like: elapsed time, elapsed ticks and time per tick.
 */
public class TimeSystem extends BaseSystem {

	private static long		timeElapsed;
	private static double	timeElapsedSeconds;
	
	private static long		ticksElapsed;

	private static long		microsPerTick;
	private static float	millisPerTick;
	
	/**
	 * Keeps track of the elpased time and provides static access to often used time values like: elapsed time, elapsed ticks and time per tick.
	 */
	public TimeSystem() {
		
		timeElapsed			= 0;
		timeElapsedSeconds	= 0;
		ticksElapsed		= 0;
	}

	@Override
	protected void processSystem() {
		
		timeElapsed += (long)(world.getDelta() * 1000f + 0.5f);

		ticksElapsed++;
		
		timeElapsedSeconds = timeElapsed / 1000d;
		
		microsPerTick = (timeElapsed * 1000L) / ticksElapsed;
		millisPerTick = microsPerTick / 1000f;
	}
	
	/**
	 * @return Time since game startup in milliseconds, rounded to one millisecond.
	 */
	public static long getCurrentTimeMillis() {
		return timeElapsed;
	}

	/**
	 * Time elapsed since game startup in seconds.
	 * @return Elapsed time as floating point number calculated from the result of {@link TimeSystem#getCurrentTimeMillis()}.
	 */
	public static double getCurrentTimeSeconds() {
		return timeElapsedSeconds;
	}

	/**
	 * @return Ticks since game startup.
	 */
	public static long getCurrentTick() {
		return ticksElapsed;
	}
	
	/**
	 * The average duration of one tick in microseconds (calculated over the whole runtime).
	 * @return Duration of one tick, rounded to a microsecond.
	 */
	public static long getMicrosPerTick() {
		return microsPerTick;
	}

	/**
	 * The average duration of one tick in seconds (calculated over the whole runtime).
	 * @return Duration of one tick as floating point number calculated from the result of {@link TimeSystem#getMicrosPerTick()}.
	 */
	public static float getMillisPerTick() {
		return millisPerTick;
	}
}
