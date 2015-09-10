package de.vatterger.entitysystem.util;

public class Timer {

	private float time;
	private float timeMax;
	
	public Timer(float timeMax) {
		reset();
		this.timeMax = GameUtil.max(timeMax, 0f);
	}
	
	public boolean tick(float delta) {
		this.time+=delta;
		return isActive();
	}
	
	public boolean isActive() {
		return !(time<timeMax);
	}
	
	public void reset() {
		time = 0f;
	}
}
