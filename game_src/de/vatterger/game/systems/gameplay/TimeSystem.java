package de.vatterger.game.systems.gameplay;

import com.artemis.BaseSystem;

public class TimeSystem extends BaseSystem {

	static long time_delta;
	
	public TimeSystem() {
		time_delta = 0;
	}

	@Override
	protected void processSystem() {
		time_delta += (long)(world.getDelta()*1000 + 0.5f);
	}
	
	public static long getCurrentTime() {
		return time_delta;
	}
}
