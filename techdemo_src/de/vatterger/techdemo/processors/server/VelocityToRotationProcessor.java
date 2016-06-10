package de.vatterger.techdemo.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;

import de.vatterger.techdemo.components.server.ServerRotation;
import de.vatterger.techdemo.components.shared.Inactive;
import de.vatterger.techdemo.components.shared.Velocity;

@Wire
public class VelocityToRotationProcessor extends EntityProcessingSystem {

	ComponentMapper<Velocity>	vm;
	ComponentMapper<ServerRotation>	rm;

	@SuppressWarnings("unchecked")
	public VelocityToRotationProcessor() {
		super(Aspect.all(ServerRotation.class, Velocity.class).exclude(Inactive.class));
	}

	protected void process(Entity e) {
		Velocity vc = vm.get(e);
		ServerRotation rc = rm.get(e);
		
		float rot = rc.rot;

		if(!vc.vel.isZero()) {
			rc.rot = MathUtils.atan2(vc.vel.y, vc.vel.x)*MathUtils.radiansToDegrees;
		}
		if(rc.rot != rot) {
			rc.newVersion();
		}
	}
}
