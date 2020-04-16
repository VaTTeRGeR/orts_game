package de.vatterger.engine.util;

import java.util.concurrent.TimeUnit;

public class Profiler {
	
	private	String nameLog;
	
	private TimeUnit timeUnitLog;
	
	private long startTime;
	private long elapsedTime;
	
	
	public Profiler(String name){
		this(name, TimeUnit.MILLISECONDS);
	}

	public Profiler(String nameLog, TimeUnit timeUnitLog) {

		if(timeUnitLog == null) timeUnitLog = TimeUnit.MILLISECONDS;

		if(nameLog == null) nameLog = "Profiler";
		
		this.timeUnitLog = timeUnitLog;
		this.nameLog = nameLog;
		
		start();
	}

	public long getTimeElapsed() {
		return System.nanoTime() - startTime;
	}
	
	public long log() {
		
		long time = getTimeElapsed();
		
		StringBuilder builder = new StringBuilder(32);
		
		builder.append("Profiler: ");
		builder.append(nameLog);
		builder.append(" [ ");
		builder.append(timeUnitLog.convert(time, TimeUnit.NANOSECONDS));
		builder.append(" ");
		builder.append(timeUnitLog.name());
		builder.append(" ]");
		
		System.out.println(builder);
		
		return time;
	}
	
	public Profiler start() {
		startTime = System.nanoTime();
		return this;
	}

	public Profiler stop() {
		elapsedTime = getTimeElapsed();
		return this;
	}
	
	public String getName() {
		return nameLog;
	}
	
	public TimeUnit getTimeUnit() {
		return timeUnitLog;
	}

	public long getMeasuredTime() {
		return getMeasuredTime(timeUnitLog);
	}
	
	public long getMeasuredTime(TimeUnit timeUnit) {
		return timeUnit.convert(elapsedTime, TimeUnit.NANOSECONDS);
	}
	
}
