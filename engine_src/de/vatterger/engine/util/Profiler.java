package de.vatterger.engine.util;

import java.util.concurrent.TimeUnit;

public class Profiler {
	private	String name;
	private long startTime;
	private TimeUnit tu;
	
	public Profiler(String name){
		this(name, TimeUnit.MILLISECONDS);
	}

	public Profiler(String name, TimeUnit tu){
		this.tu = tu;
		this.name = name;
		start();
	}

	public long getTimeElapsed(){
		return System.nanoTime()-startTime;
	}
	
	public long log(){
		long time = getTimeElapsed();
		
		StringBuilder builder = new StringBuilder(32);
		
		builder.append("Profiler: ");
		builder.append(name);
		builder.append(" [ ");
		builder.append(tu.convert(time, TimeUnit.NANOSECONDS));
		builder.append(" ");
		builder.append(tu.name());
		builder.append(" ]");
		System.out.println(builder);
		
		return time;
	}
	
	public Profiler start(){
		startTime = System.nanoTime();
		return this;
	}
}
