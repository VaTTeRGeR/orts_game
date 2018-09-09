package de.vatterger.engine.handler.pathfinding;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.badlogic.gdx.math.Vector3;

public class PathFindingRequest {
	
	protected int entityId = -1;
	
	protected  Vector3 start = null;
	protected  Vector3 end = null;
	
	protected long timeout = 100;
	
	protected Consumer<ArrayList<Vector3>> finishCallback = null;
	
	protected boolean finished = false;
	
	protected  ArrayList<Vector3> path = null;

	public PathFindingRequest(int entityId, Vector3 start, Vector3 end) {
		this.entityId = entityId;
		this.start = start.cpy();
		this.end = end.cpy();
	}
	
	public PathFindingRequest withTimeout(long timeout) {
		
		if(timeout > 1) {
			this.timeout = timeout;
		}
		
		return this;
	}
	
	public PathFindingRequest withFinishCallback(Consumer<ArrayList<Vector3>> finishCallback) {
		this.finishCallback = finishCallback;
		return this;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public  ArrayList<Vector3> getPath() {
		return path;
	}
}
