package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.entitysystem.components.server.RemoteMaster;

public class RemoteMasterGracePeriodProcessor extends IteratingSystem {

	private static ComponentMapper<RemoteMaster> rmm;

	public RemoteMasterGracePeriodProcessor() {
		super(Aspect.all(RemoteMaster.class));
	}

	@Override
	protected void process(int e) {
		RemoteMaster rm = rmm.get(e);
		if(rm.deltaDelay >= 0f) {
			rm.deltaDelay -= world.getDelta();
		}
	}
}
