package de.vatterger.threadedSim;

import de.vatterger.entitysystem.EntitySystemWorld;
import de.vatterger.threadedSim.tools.ProfileUnit;
import de.vatterger.threadedSim.tools.Profiler;

public class Main {
	public static void main(String[] args) {
		final Profiler p = new Profiler("Main Thread", ProfileUnit.SECONDS);

		final int numThreads = 1;
		
		UpdateRunnable runnable[] = new UpdateRunnable[numThreads];
		
		for (int i = 0; i < runnable.length; i++) {
			runnable[i] = new UpdateRunnable(new EntitySystemWorld());
			runnable[i].setName("Runnable "+i);
		}
		
		
		for (int i = 0; i < runnable.length; i++) {
			runnable[i].setDebug(false);
			runnable[i].startSimulation();
		}

		/*LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(10L, TimeUnit.SECONDS));

		System.out.println("Times up, Shutdown initiated!\n");

		for (int i = 0; i < runnable.length; i++) {
			//runnable[i].stopSimulation();
		}*/
		p.logTimeElapsed();
	}
}
