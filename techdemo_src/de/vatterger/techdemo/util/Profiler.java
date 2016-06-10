package de.vatterger.techdemo.util;

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
		System.out.println("Profiler: "+name+" [ "+tu.convert(time, TimeUnit.NANOSECONDS)+" "+tu.name()+" ]");
		return time;
	}
	
	public Profiler start(){
		startTime = System.nanoTime();
		return this;
	}
}
