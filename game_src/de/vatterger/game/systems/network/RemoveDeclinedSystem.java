package de.vatterger.game.systems.network;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;

import de.vatterger.game.components.client.ClientDeclined;

public class RemoveDeclinedSystem extends IteratingSystem {
	
	
	public RemoveDeclinedSystem() {
		super(Aspect.all(ClientDeclined.class));
	}

	@Override
	protected void process(int e) {
		world.delete(e);
	}
}
