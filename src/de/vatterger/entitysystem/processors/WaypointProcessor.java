package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.ServerPosition;
import de.vatterger.entitysystem.components.ServerRotation;
import de.vatterger.entitysystem.components.Velocity;
import de.vatterger.entitysystem.components.WaypointPath;

@Wire
public class WaypointProcessor extends EntityProcessingSystem {

	ComponentMapper<Velocity>	vm;
	ComponentMapper<ServerPosition>	spm;
	ComponentMapper<WaypointPath>	wpm;

	@SuppressWarnings("unchecked")
	public WaypointProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, Velocity.class, WaypointPath.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		Velocity vc = vm.get(e);
		ServerPosition spc = spm.get(e);
		WaypointPath wpc = wpm.get(e);
		
		Vector3 target = wpc.waypoints.peek();
		if(target == null) {
			e.edit().remove(wpc);
			vc.vel.setZero();
		} else if(target.epsilonEquals(spc.pos, vc.vel.len()*world.getDelta())) {
			vc.vel.setZero();
			wpc.waypoints.remove();
			if(wpc.repeat) {
				wpc.waypoints.add(target);
			}
		} else {
			float speed = 20f;
			Vector3 dir = target.cpy().sub(spc.pos).nor();
			vc.vel.set(dir.scl(speed));
		}
	}
}
