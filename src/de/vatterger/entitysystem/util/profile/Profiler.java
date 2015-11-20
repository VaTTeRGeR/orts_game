package de.vatterger.entitysystem.util.profile;

import java.util.concurrent.TimeUnit;

public class Profiler {
	private	String name;
	private long startTime;
	private TimeUnit pu;
	
	public Profiler(String name){
		this(name, TimeUnit.MILLISECONDS);
	}

	public Profiler(String name, TimeUnit tu){
		this.pu = tu;
		this.name = name;
		start();
	}

	public long getTimeElapsed(){
		return System.nanoTime()-startTime;
	}
	
	public long log(){
		long time = getTimeElapsed();
		System.out.println("Profiler: "+name+" ["+time/pu.convert(time, TimeUnit.NANOSECONDS)+pu.name()+"]");
		return time;
	}
	
	public Profiler start(){
		startTime = System.nanoTime();
		return this;
	}
}
