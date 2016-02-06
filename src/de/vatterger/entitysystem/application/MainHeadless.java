package de.vatterger.entitysystem.application;

import java.io.IOException;

public class MainHeadless {
	private static UpdateRunnable runnable;
	
	public static void main(String[] args) {
		/*Creating the simulation-thread*/
		runnable = new UpdateRunnable(new BattleServer());
		
		/*The target-framerate is 20 steps per second*/
		runnable.setTargetFPS(20);
		
		start();
		while (true) {
			try {
				byte[] b = new byte[32];
				String s = null;
				System.in.read(b);
				System.out.println("< "+(s = new String(b).trim()));
				if(s.equals("quit") || s.equals("exit") || s.equals("stop") || s.equals("kill") || s.equals("shutdown") || s.equals("q")){
					System.out.println("Stopping server.");
					stop();
					System.exit(0);
				} else if(s.equals("pause") || s.equals("p")) {
					pause();
					System.out.println("Server paused: "+runnable.isTimeFreeze());
				} else if(s.equals("debug") || s.equals("d")) {
					debug();
					System.out.println("Runnable Debugging: "+runnable.isDebug());
				} else {
					System.out.println("Help:");
					System.out.println(" q/quit/exit to shutdown");
					System.out.println(" p/pause to pause/unpause");
					System.out.println(" d/debug to toggle debug");
					System.out.println("\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
				
	private static void start() {
		runnable.setDebug(false);
		runnable.startSimulation();
	}

	private static void debug() {
		runnable.setDebug(!runnable.isDebug());
	}

	private static void pause() {
		runnable.setTimeFreeze(!runnable.isTimeFreeze());
	}

	private static void stop() {
		runnable.stopSimulation();
	}
	
	@Override
	protected void finalize() throws Throwable {
		stop();
	}
	
	public static void printConsole(String s) {
		System.out.println(s+"\n");
	}

	public static void clearConsole() {
		System.out.println("\n\n");
	}
}