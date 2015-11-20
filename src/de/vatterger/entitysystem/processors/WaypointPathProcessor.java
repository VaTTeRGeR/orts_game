package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.WaypointPath;
import de.vatterger.entitysystem.components.WaypointTarget;

@Wire
public class WaypointPathProcessor extends EntityProcessingSystem {

	ComponentMapper<WaypointPath>	wppm;
	ComponentMapper<WaypointTarget>	wptm;

	@SuppressWarnings("unchecked")
	public WaypointPathProcessor() {
		super(Aspect.getAspectForAll(WaypointPath.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		WaypointPath wppc = wppm.get(e);
		WaypointTarget wptc = wptm.getSafe(e);
		
		Vector3 target = wppc.waypoints.peek();
		if(target == null) {
			e.edit().remove(wppc);
			if(wptc != null) {
				e.edit().remove(wptc);
			}
		} else if(wptc == null) {
			e.edit().add(new WaypointTarget(target));
			wppc.waypoints.remove();
			if(wppc.repeat) {
				wppc.waypoints.add(target);
			}
		} else {
			wptc.waypoint.set(target);
		}
	}
}
