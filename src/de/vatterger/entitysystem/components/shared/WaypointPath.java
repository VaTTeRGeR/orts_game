package de.vatterger.entitysystem.components.shared;


import java.util.LinkedList;
import java.util.Queue;

import com.artemis.Component;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.interfaces.Versionable;
import de.vatterger.entitysystem.interfaces.Sizeable;

public class WaypointPath extends Component implements Versionable, Sizeable {
	public Queue<Vector3> waypoints = new LinkedList<Vector3>();
	public boolean repeat = false;

	public WaypointPath() {
	}
	
	public WaypointPath(Bag<Vector3> waypoints, boolean repeat) {
		this((Vector3[])waypoints.getData(), repeat);
	}

	public WaypointPath(Vector3[] waypoints, boolean repeat) {
		for (Vector3 vector : waypoints) {
			this.waypoints.add(vector);
		}
		this.repeat = repeat;
	}

	private int v = 0;
	@Override
	public void newVersion() {
		v++;
		if(v == Integer.MAX_VALUE) {
			v = 0;
		}
	}

	@Override
	public int getVersion() {
		return v;
	}

	@Override
	public boolean compareVersion(int v2) {
		return v != v2;
	}

	@Override
	public int getSizeInBytes() {
		return 3*2*waypoints.size()+1; // 3 x var float x waypoints-size + overhead
	}
}
