package de.vatterger.threadedSim;
import java.util.Date;
import java.util.concurrent.locks.LockSupport;

import de.vatterger.entitysystem.interfaces.Nameable;
import de.vatterger.entitysystem.interfaces.World;

public class UpdateRunnable implements Runnable, Nameable {

	World w;
	String name;
	
	boolean run,//should the gameloop continue to iterate
			save,//is the game state being saved currently
			debugFine;//print advanced debug messages
	
	float 	deltaSeconds;//The delta-value passed to the entity-system for simulation (=n*deltaNanos)
	
	long	updateFps,//The wanted frames per second
			wantedDeltaNanos,//How long an gameloop-iteration should ideally take (=n/updateFps)
			deltaNanos,//The accurate version of the time passed to the simulation

			updateNanosConsumed,//How long the entity-system took to update
			
			wantedSleep,//How long the System should have paused
			actualSleep,//How long the System actually paused
			deviation,//How much actualSleep differs from wantedSleep
			
			elapsedTotal,//How long the Execution took
			temp;//Temporary Variable to measure time
	
	public UpdateRunnable(World w) {
		setIsRunning(false);
		setDebug(false);
		save = false;

		this.w = w;
		
		updateFps = 100;
		wantedDeltaNanos = (long)((1f/updateFps)*1000000000);
		deltaNanos = 0;
		
		elapsedTotal = 0;
		deviation = 0;
				
		System.out.println("Thread created\n");
	}

	@Override
	public void run() {
		Date beginDate = new Date();

		System.out.println("Thread starting up\n");
		System.out.println("Thread started at: " + beginDate + "\n");

		try {
			w.create();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long tempTest = System.nanoTime();
		while (isRunning()) {
			deltaSeconds = (float)(deltaNanos/1000000000d);//The deltatime passed to the worlds update-function
			temp = System.nanoTime();//Current Nanotime (before game logic execution)

			if(debugFine) {
				System.out.println("Thread updated with delta = "+deltaSeconds*1000f+"ms");
				System.out.println("Updating took: "+updateNanosConsumed/1000000f+"ms");
				System.out.println("Wanted Sleeptime: "+wantedSleep/1000000f+"ms");
				System.out.println("Actual Sleeptime: "+actualSleep/1000000f+"ms");
				System.out.println("Deviation: "+deviation/1000000f+"ms"+"\n");
			}
			
			w.update(deltaSeconds);//Update the World
			
			updateNanosConsumed = System.nanoTime()-temp;//How long the updating took
			
			temp = System.nanoTime();//Current Nanotime (after game logic execution)
			
			wantedSleep = wantedDeltaNanos - updateNanosConsumed + deviation;//How long the Thread should ideally pause
			
			if(0L < wantedSleep) {//decide if update-time is in range of (0, Long.MAXVALUE)
				LockSupport.parkNanos(wantedSleep);//Try to pause the Thread for fitting amount of time
			}

			actualSleep = System.nanoTime()-temp;//How long the pause actually lasted
			deviation = wantedSleep-actualSleep;//This time difference needs to be made up for in the next iteration
			deltaNanos = updateNanosConsumed + actualSleep;//the total time needed for this iteration is passed to the update-function in the next frame
			
			elapsedTotal+=deltaNanos;//Measure how long the execution has been going since startup
			if(elapsedTotal>10L*1000L*1000000L)
				stopSimulation();
		}
		tempTest = System.nanoTime()-tempTest;
		System.out.println("Total simulation-time: " + tempTest/1000000f+"ms \n");
		
		w.dispose();
		
		Date finishDate = new Date();
		System.out.println("Thread "+name+" finished at: " + finishDate + ", took "+elapsedTotal/1000000f+"ms \n");
		//System.out.println("Thread "+name+" finished at: " + finishDate + ", took "+( finishDate.getTime()-beginDate.getTime())+"ms \n");
	}
		
	public void setDebug(boolean debugOn){
		debugFine = debugOn;
	}
	
	public boolean getDebug(){
		return debugFine;
	}
	
	public void stopSimulation() {
		setIsRunning(false);
	}

	public void startSimulation() {
		setIsRunning(true);
		new Thread(this).start();
	}
	
	public boolean isRunning(){
		return run;
	}
	
	public void setIsRunning(boolean run) {
		this.run = run;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		if(isRunning()) {
			return "Thread: "+getName()+". Running World: "+w.toString();
		} else {
			return "Thread: "+getName()+". Idling World: "+w.toString();
		}
	}
}
