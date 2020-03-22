package de.vatterger.game.systems.gameplay;

import com.artemis.BaseSystem;

/**
 * Keeps track of the elapsed time and provides static access to often used time values like: elapsed time, elapsed ticks and time per tick.
 */
public class TimeSystem extends BaseSystem {

	private static long		elapsedMillis;
	private static double	elapsedSeconds;
	
	private static long		elapsedTicks;

	private static long		microsPerTick;
	private static float		millisPerTick;
	
	/**
	 * Keeps track of the elapsed time and provides static access to often used time values like: elapsed time, elapsed ticks and time per tick.
	 */
	public TimeSystem() {
		
		elapsedMillis	= 0;
		elapsedSeconds	= 0;
		elapsedTicks	= 0;
	}

	@Override
	protected void processSystem() {
		

		elapsedSeconds += (double)world.getDelta();
		elapsedMillis = (long)(elapsedSeconds * 1000f + 0.5f);

		elapsedTicks++;
		
		
		microsPerTick = (elapsedMillis * 1000L) / elapsedTicks;
		millisPerTick = microsPerTick / 1000f;
	}
	
	/**
	 * @return Time since game startup in milliseconds, rounded to one millisecond.
	 */
	public static long getCurrentTimeMillis() {
		return elapsedMillis;
	}

	/**
	 * Time elapsed since game startup in seconds.
	 * @return Elapsed time as floating point number calculated from the result of {@link TimeSystem#getCurrentTimeMillis()}.
	 */
	public static double getCurrentTimeSeconds() {
		return elapsedSeconds;
	}

	/**
	 * @return Ticks since game startup.
	 */
	public static long getCurrentTick() {
		return elapsedTicks;
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
