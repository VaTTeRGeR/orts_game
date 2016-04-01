package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.VehicleProperties;
import de.vatterger.entitysystem.components.shared.Velocity;
import de.vatterger.entitysystem.components.shared.WaypointTarget;

@Wire
public class WaypointTargetProcessor extends EntityProcessingSystem {

	private ComponentMapper<Velocity>	vm;
	private ComponentMapper<ServerPosition>	spm;
	private ComponentMapper<WaypointTarget>	wptm;
	private ComponentMapper<VehicleProperties>	vpm;
	
	private Vector3 dir = new Vector3();

	@SuppressWarnings("unchecked")
	public WaypointTargetProcessor() {
		super(Aspect.all(ServerPosition.class, Velocity.class, WaypointTarget.class, VehicleProperties.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		Velocity vc = vm.get(e);
		ServerPosition spc = spm.get(e);
		WaypointTarget wptc = wptm.get(e);
		VehicleProperties vpc = vpm.get(e);
		
		if(spc.pos.epsilonEquals(wptc.waypoint, vc.vel.len()*world.getDelta()) || wptc.waypoint == null) {
			e.edit().remove(wptc);
			vc.vel.setZero();
			spc.newVersion();
		} else {
			dir.set(wptc.waypoint).sub(spc.pos).nor();
			vc.vel.set(dir.scl(vpc.speed_max));
		}
	}
}
