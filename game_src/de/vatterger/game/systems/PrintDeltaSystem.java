package de.vatterger.game.systems;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;

public class PrintDeltaSystem extends IteratingSystem {

	public PrintDeltaSystem() {
		super(Aspect.all());
	}

	@Override
	protected void process(int e) {
		System.out.println("Processing " + e + " with " + world.getDelta());
	}
}
