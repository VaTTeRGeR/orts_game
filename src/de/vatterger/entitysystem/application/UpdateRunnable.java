package de.vatterger.entitysystem.application;
import java.util.Date;
import java.util.concurrent.locks.LockSupport;

import de.vatterger.entitysystem.interfaces.UpdateableWorld;

public class UpdateRunnable implements Runnable {

	/**The World Instance*/
	UpdateableWorld w;
	
	boolean run,//should the gameloop continue to iterate
			debugFine;//print advanced debug messages
	
	float 	deltaSeconds,//The delta-value passed to the entity-system for simulation (=n*deltaNanos)
			wantedDeltaSeconds,//The delta-value passed to the entity-system for fixed-timestep simulation (=deltaNanos/10^9)
			updateFps;//The current frames per second
	
	int		wantedUpdateFps;//The wanted frames per second

	long	wantedDeltaNanos,//How long an gameloop-iteration should ideally take (=n/updateFps)
			deltaNanos,//The accurate version of the time passed to the simulation

			updateNanosConsumed,//How long the entity-system took to update
			
			wantedSleep,//How long the System should have paused
			actualSleep,//How long the System actually paused
			deviation,//How much actualSleep differs from wantedSleep
			
			elapsedTotal,//How long the Execution took
			temp;//Temporary Variable to measure time
	
	public UpdateRunnable(UpdateableWorld w) {
		setIsRunning(false);
		setDebug(false);

		this.w = w;
		
		setTargetFPS(20);
		
		deltaNanos = 0;
		
		elapsedTotal = 0;
		deviation = 0;
	}
	
	@Override
	public void run() {
		Date beginDate = new Date();

		if(debugFine) {
			System.out.println("Thread starting up\n");
			System.out.println("Thread started at: " + beginDate + "\n");
		}

		try {
			w.create();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long tempTest = System.nanoTime();
		while (isRunning()) {
			deltaSeconds = (float)(deltaNanos/1000000000d);//The deltatime passed to the worlds update-function
			updateFps = 1f/deltaSeconds;
			temp = System.nanoTime();//Current Nanotime (before game logic execution)

			if(debugFine) {
				System.out.println("Thread updated with delta = "+deltaSeconds*1000f+"ms");
				System.out.println("Updating took: "+updateNanosConsumed/1000000f+"ms");
				System.out.println("Wanted Sleeptime: "+wantedSleep/1000000f+"ms");
				System.out.println("Actual Sleeptime: "+actualSleep/1000000f+"ms");
				System.out.println("Deviation: "+deviation/1000000f+"ms"+"\n");
			}
			
			//w.update(wantedDeltaSeconds);//Update the World with variable timestep
			w.update(deltaSeconds);//Update the World with variable timestep
			
			updateNanosConsumed = System.nanoTime()-temp;//How long the updating took
			
			temp = System.nanoTime();//Current Nanotime (after game logic execution)
			
			wantedSleep = wantedDeltaNanos - updateNanosConsumed + deviation;//How long the Thread should ideally pause
			
			if(0L < wantedSleep) {//decide if update-time is in range of (0, Long.MAXVALUE)
				LockSupport.parkNanos(wantedSleep);//Try to pause the Thread for fitting amount of time
			}

			actualSleep = System.nanoTime()-temp;//How long the pause actually lasted
			deviation = wantedSleep-actualSleep;//This time difference needs to be made up for in the next iteration
			deltaNanos = updateNanosConsumed + actualSleep;//the total time needed for this iteration, it is passed to the update-function in the next frame
			
			elapsedTotal+=deltaNanos;//Measure how long the execution has been going since startup
		}
		tempTest = System.nanoTime()-tempTest;

		w.dispose();
		
		Date finishDate = new Date();
		if(debugFine) {
			System.out.println("Total simulation-time: " + tempTest/1000000f+"ms \n");
			System.out.println("Thread finished at: " + finishDate + " ["+elapsedTotal/1000000f+"ms]\n");
		}
	}
	
	public void setDebug(boolean debugOn){
		debugFine = debugOn;
	}
	
	public boolean getIsDebug(){
		return debugFine;
	}
	
	public void stopSimulation() {
		setIsRunning(false);
	}

	public void startSimulation() {
		setIsRunning(true);
		Thread t = 	new Thread(this);
		t.setName("Game-UpdateRunnable-Game Container");
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
	}
	
	private boolean isRunning(){
		return run;
	}
	
	private void setIsRunning(boolean run) {
		this.run = run;
	}
	
	public float getCurrentFPS(){
		return updateFps;
	}
	
	public void setTargetFPS(int fps){
		wantedUpdateFps = fps;
		wantedDeltaSeconds = 1f/fps;
		wantedDeltaNanos = (long)((1f/wantedUpdateFps)*1000000000);
	}
	
	public UpdateableWorld getWorld(){
		return this.w;
	}

	@Override
	public String toString() {
		if(isRunning()) {
			return "UpdateRunnable running World: "+w.toString();
		} else {
			return "UpdateRunnable idling World: "+w.toString();
		}
	}
}
