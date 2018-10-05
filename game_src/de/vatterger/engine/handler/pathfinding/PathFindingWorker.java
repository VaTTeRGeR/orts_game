package de.vatterger.engine.handler.pathfinding;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;

@SuppressWarnings("serial")
public class PathFindingWorker extends ArrayBlockingQueue<PathFindingRequest> implements Runnable {
	
	private volatile boolean run = true;
	
	private PathFinder pathFinder = new PathFinder();
	
	private Thread thread = null;
	
	public PathFindingWorker() {
		super(2048);
		
		thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}
	
	@Override
	public void run() {
		while(run && !Thread.currentThread().isInterrupted()) {
			
			PathFindingRequest request = null;
			
			try {
				request = poll(5, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			final PathFindingRequest requestFinal = request;
			
			if(requestFinal != null) {
				
				requestFinal.path = pathFinder.createPath(requestFinal.start, requestFinal.end , requestFinal.timeout);

				if(requestFinal.finishCallback != null) {
					Gdx.app.postRunnable(() -> {
						requestFinal.finishCallback.accept(requestFinal.path);
					});
				}

				requestFinal.finished = true;
			}
		}
	}
	
	public void stop() {

		run = false;
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
