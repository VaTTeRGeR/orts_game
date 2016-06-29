package de.vatterger.game.systems;

import com.artemis.BaseSystem;

public class PrintDeltaSystem extends BaseSystem {

	@Override
	protected void processSystem() {
		System.out.println("Processing with " + world.getDelta());
	}
}
