package de.vatterger.engine.handler.pathfinding;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntArray;

@SuppressWarnings("serial")
public class PathFindingWorker extends ArrayBlockingQueue<PathFindingRequest> implements Runnable {
	
	private IntArray entities = new IntArray(true,2048);
	
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
	public boolean offer(PathFindingRequest e) {
		
		if(entities.contains(e.entityId)) {
			for (PathFindingRequest req : this) {
				if(req.entityId == e.entityId) {
					req.cancel = true;
				}
			}
		} else {
			entities.add(e.entityId);
		}
		
		boolean offerAccepted = super.offer(e);
		
		if(!offerAccepted) {
			entities.removeValue(e.entityId);
		}
		
		return offerAccepted;
	}
	
	@Override
	public void run() {
		while(run && !Thread.currentThread().isInterrupted()) {
			
			PathFindingRequest tryRequest = null;
			
			try {
				tryRequest = poll(5, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			final PathFindingRequest request = tryRequest;
			
			if(request != null && !request.cancel) {
				
				entities.removeValue(request.entityId);
				
				request.path = pathFinder.createPath(request.start, request.end , request.timeout);

				if(request.finishCallback != null) {
					Gdx.app.postRunnable(() -> {
						request.finishCallback.accept(request.path);
					});
				}
				
				request.finished = true;
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
