package de.vatterger.entitysystem.tools;

public class Profiler {
	private	String name;
	private long startTime;
	private ProfileUnit pu;
	
	public Profiler(String name){
		this(name, ProfileUnit.MILLISECONDS);
	}

	public Profiler(String name, ProfileUnit tu){
		this.pu = tu;
		this.name = name;
		start();
	}

	public long getTimeElapsed(){
		return System.nanoTime()-startTime;
	}
	
	public long logTimeElapsed(){
		long time = getTimeElapsed();
		System.out.println("Profiler: "+name+" ["+time/pu.scale+pu.identifier+"]");
		return time;
	}
	
	public Profiler start(){
		startTime = System.nanoTime();
		return this;
	}
}
