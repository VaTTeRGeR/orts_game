package de.vatterger.game.systems;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;

public class PrintDeltaSystem extends BaseSystem {

	@Override
	protected void processSystem() {
		System.out.println("Processing with " + world.getDelta() + " / " + (Gdx.graphics.getFramesPerSecond()) + "fps");
	}
}
