package de.vatterger.engine.handler.pathfinding;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;

import com.badlogic.gdx.utils.IntArray;

import de.vatterger.engine.util.AtomicRingBuffer;

@SuppressWarnings("serial")
public class PathFindingWorker extends AtomicRingBuffer<PathFindingRequest> implements Runnable {
	
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

			while(this.has()) {
				
				PathFindingRequest req = this.get();
				
				checkAndAddRequest(req);
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
				
				AtomicRingBuffer<PathFindingRequest> returnQueue = request.returnQueue;
				
				if(returnQueue != null) {

					while(!returnQueue.canWrite()) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
					
					returnQueue.put(request);
					
					returnQueue = null;
				}
				
				Thread.yield();
				
			} else {
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
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
