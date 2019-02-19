package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import de.vatterger.game.systems.gameplay.TimeSystem;

public class MoveCurve extends Component {
	
	public Array<Vector3>	pathPoints 		= null;
	public Array<Long>		pathTimes		= null;
	
	public MoveCurve() {}
	
	public MoveCurve(Vector3[] pathPoints, MovementParameters moveParams) {
		this(pathPoints, TimeSystem.getCurrentTimeMillis(), moveParams);
	}
	
	public MoveCurve(Vector3[] path, long startTime, MovementParameters moveParams) {
		//System.out.println(Arrays.toString(pathPoints) + ", length: " + pathPoints.length);
		
		pathPoints		= new Array<Vector3>(path);
		pathTimes		= new Array<Long>(path.length);
		
		pathTimes.add(startTime);
		
		long baseTime = startTime;
		
		for (int i = 1; i < path.length; i++) {
			float distance = path[i-1].dst(path[i]);
			baseTime = baseTime + (long)(1000*(distance/moveParams.forwardSpeed));
			pathTimes.add(baseTime);
		}
		
		if(moveParams == null) {
			return;
		}
		
		/*long delaySum = 0L;
		
		for (int i = 1; i < path.length - 1; i++) {
			
			Vector2 v0 = new Vector2(path[i-1].x, path[i-1].y);
			Vector2 v1 = new Vector2(path[i].x, path[i].y);
			Vector2 v2 = new Vector2(path[i+1].x, path[i+1].y);
			
			v2.sub(v1).nor();
			v1.sub(v0).nor();
			
			float angle = v1.angle(v2);
			float angleAbs = Math.abs(angle);
		}*/
	}
	
	public MoveCurve(Vector3[] pathPoints, Long[] timePoints) {
		if(pathPoints != null && pathPoints.length > 0 && timePoints != null && timePoints.length == pathPoints.length) {
			this.pathPoints	= new Array<Vector3>(pathPoints);
			this.pathTimes	= new Array<Long>(timePoints);
		} else {
			this.pathPoints = new Array<Vector3>(0);
		}
	}
}
