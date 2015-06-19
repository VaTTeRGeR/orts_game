package de.vatterger.threadedSim;

import de.vatterger.entitysystem.EntitySystemWorld;
import de.vatterger.threadedSim.tools.ProfileUnit;
import de.vatterger.threadedSim.tools.Profiler;

public class Main {
	public static void main(String[] args) {
		System.out.println("ServerCMD started, press enter to see available commands...");

		final Profiler p = new Profiler("Main Thread", ProfileUnit.SECONDS);
		SERVER_STATUS runStatus = SERVER_STATUS.Idle;
		final int numThreads = 1;
		boolean debug = false;
		final UpdateRunnable runnable[] = new UpdateRunnable[numThreads];

		for (int i = 0; i < runnable.length; i++) {
			runnable[i] = new UpdateRunnable(new EntitySystemWorld());
			runnable[i].setName("Runnable " + i);
		}
		
		String str;
		byte[] b;
		while(runStatus != SERVER_STATUS.Exit) {
			try {
				b = new byte[256];
				int length = System.in.read(b);
				str = new String(b,0,length-2);
				System.out.println("\""+str+"\"");
				switch (str) {
					
					case "start":
						if (runStatus == SERVER_STATUS.Idle) {
							runStatus = SERVER_STATUS.Run;
							System.out.println("Starting Server");

							for (int i = 0; i < runnable.length; i++) {
								runnable[i].setDebug(debug);
								runnable[i].startSimulation();
							}
						} else {
							System.out.println("Server already started.");
						}
					break;
					case "exit":
					case "quit":
					case "shutdown":
						runStatus = SERVER_STATUS.Exit;
						System.out.println("Shutting down...");
					
					case "stop":
						if(runStatus != SERVER_STATUS.Idle) {
							System.out.println("Stopping server");
							for (int i = 0; i < runnable.length; i++) {
								runnable[i].stopSimulation();
							}
							if(runStatus == SERVER_STATUS.Run) {
								runStatus = SERVER_STATUS.Idle;
							}
						} else {
							System.out.println("No server running");
						}
					break;
					
					case "fps":
						final String sCL;
						if(runStatus == SERVER_STATUS.Run)
							sCL = "Current";
						else
							sCL = "Last";
						for (int i = 0; i < runnable.length; i++) {
							System.out.println(sCL + " FPS on Thread "+i+": "+runnable[i].getFPS());
						}
					break;
					
					case "mem":
						int mb = 1024 * 1024;

						Runtime runtime = Runtime.getRuntime();

						System.out.println("##### Heap utilization statistics [MB] #####");
						System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
						System.out.println("Free Memory:" + runtime.freeMemory() / mb);
						System.out.println("Total Memory:" + runtime.totalMemory() / mb);
						System.out.println("Max Memory:" + runtime.maxMemory() / mb);
					break;
					
					case "debug":
						debug = !debug;
						for (int i = 0; i < runnable.length; i++) {
							runnable[i].setDebug(debug);
							if(runnable[i].getDebug())
								System.out.println("Debug enabled.");
							else
								System.out.println("Debug disabled.");
						}
					break;
					
					default:
						System.out.println("[start|stop|debug|fps|mem|exit/shutdown/quit]");
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		p.logTimeElapsed();
	}
}
enum SERVER_STATUS {Run,Idle,Exit}