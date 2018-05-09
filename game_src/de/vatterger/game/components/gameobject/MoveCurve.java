package de.vatterger.game.components.gameobject;

import java.util.Arrays;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class MoveCurve extends Component {
	
	public Array<Vector3>	pathPoints  = null;
	public Array<Long>		pathTimes	= null;
	
	public MoveCurve() {}
	
	public MoveCurve(Vector3[] pathPoints, float speed, long startTime) {
		System.out.println(Arrays.toString(pathPoints) + ", length: " + pathPoints.length);
		
		this.pathPoints = new Array<Vector3>(pathPoints);
		pathTimes = new Array<Long>(pathPoints.length);
		
		pathTimes.add(startTime);
		
		long baseTime = startTime;
		
		for (int i = 1; i < pathPoints.length; i++) {
			float distance = pathPoints[i-1].dst(pathPoints[i]);
			baseTime = baseTime + (long)(1000*(distance/speed));
			pathTimes.add(baseTime);
		}
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
