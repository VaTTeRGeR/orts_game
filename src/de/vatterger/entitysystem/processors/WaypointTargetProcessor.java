package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.Velocity;
import de.vatterger.entitysystem.components.shared.WaypointTarget;

@Wire
public class WaypointTargetProcessor extends EntityProcessingSystem {

	private ComponentMapper<Velocity>	vm;
	private ComponentMapper<ServerPosition>	spm;
	private ComponentMapper<WaypointTarget>	wptm;
	
	private Vector3 dir = new Vector3();

	private final float DEBUG_SPEED = 5f;
	
	@SuppressWarnings("unchecked")
	public WaypointTargetProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, Velocity.class, WaypointTarget.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		Velocity vc = vm.get(e);
		ServerPosition spc = spm.get(e);
		WaypointTarget wptc = wptm.get(e);
		
		if(spc.pos.epsilonEquals(wptc.waypoint, vc.vel.len()*world.getDelta()) || wptc.waypoint == null) {
			e.edit().remove(wptc);
			vc.vel.setZero();
		} else {
				
			dir.set(wptc.waypoint).sub(spc.pos).nor();
			vc.vel.set(dir.scl(DEBUG_SPEED));
		}
		spc.newVersion();
	}
}
