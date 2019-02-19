package de.vatterger.engine.handler.pathfinding;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;

import com.badlogic.gdx.utils.IntArray;

@SuppressWarnings("serial")
public class PathFindingWorker extends ArrayBlockingQueue<PathFindingRequest> implements Runnable {
	
	private IntArray entities = new IntArray(true,2048);
	
	private LinkedList<PathFindingRequest> requests = new LinkedList<>();
	
	private volatile boolean run = true;
	
	private CircleTracePathFinder pathFinder = new CircleTracePathFinder();
	
	private Thread thread = null;
	
	public PathFindingWorker() {
		super(2048);
		
		thread = new Thread(this);
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	private void checkAndAddRequest(PathFindingRequest request) {

		if(entities.contains(request.entityId)) {
			for (PathFindingRequest r : requests) {
				if(r.entityId == request.entityId) {
					r.cancel = true;
				}
			}
		} else {
			entities.add(request.entityId);
		}

		requests.add(request);
	}
	
	@Override
	public void run() {
		
		while(run && !Thread.currentThread().isInterrupted()) {

			if(!this.isEmpty()) {
				
				LinkedList<PathFindingRequest> drainList = new LinkedList<>();

				this.drainTo(drainList);

				for (PathFindingRequest request : drainList) {
					checkAndAddRequest(request);
				}
			}
			
			final PathFindingRequest request = requests.poll();
			
			if(request != null && !request.cancel) {
				
				entities.removeValue(request.entityId);
				
				request.path = pathFinder.createPath(request.start, request.end , request.timeout);

				/*if(request.finishCallback != null) {
					Gdx.app.postRunnable(() -> {
						request.finishCallback.accept(request.path);
					});
				}*/
				
				request.finished = true;
				
				if(request.returnQueue != null) {

					try {
						request.returnQueue.put(request);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					request.returnQueue = null;
				}
			} else {
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
