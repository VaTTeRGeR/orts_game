package de.vatterger.entitysystem.components.shared;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class WaypointTarget extends Component {
	public Vector3 waypoint = new Vector3();

	public WaypointTarget() {
	}
	
	public WaypointTarget(Vector3 waypoint) {
		this.waypoint.set(waypoint);
	}
}
