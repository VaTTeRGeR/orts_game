package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.ServerPosition;
import de.vatterger.entitysystem.components.Velocity;
import de.vatterger.entitysystem.components.WaypointTarget;

@Wire
public class WaypointTargetProcessor extends EntityProcessingSystem {

	ComponentMapper<Velocity>	vm;
	ComponentMapper<ServerPosition>	spm;
	ComponentMapper<WaypointTarget>	wptm;

	@SuppressWarnings("unchecked")
	public WaypointTargetProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, Velocity.class, WaypointTarget.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		Velocity vc = vm.get(e);
		ServerPosition spc = spm.get(e);
		WaypointTarget wptc = wptm.get(e);
		
		if(spc.pos.epsilonEquals(wptc.waypoint, vc.vel.len()*world.getDelta())){
			e.edit().remove(wptc);
			vc.vel.setZero();
		} else {
			float speed = 10f;
			Vector3 dir = wptc.waypoint.cpy().sub(spc.pos).nor();
			vc.vel.set(dir.scl(speed));
		}
	}
}
