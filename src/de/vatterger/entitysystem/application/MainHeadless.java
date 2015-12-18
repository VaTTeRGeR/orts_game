package de.vatterger.entitysystem.application;

import de.vatterger.entitysystem.BattleServer;

public class MainHeadless {
	private static UpdateRunnable runnable;
	
	public static void main(String[] args) {
		/*Creating the simulation-thread*/
		runnable = new UpdateRunnable(new BattleServer());
		
		/*The target-framerate is 20 steps per second*/
		runnable.setTargetFPS(20);
		
		start();
	}
				
	private static void start() {
		runnable.setDebug(false);
		runnable.startSimulation();
	}
	
	@Override
	protected void finalize() throws Throwable {
		runnable.stopSimulation();
	}
	
	public static void printConsole(String s) {
		System.out.println(s+"\n");
	}

	public static void clearConsole() {
		System.out.println("\n\n");
	}
}