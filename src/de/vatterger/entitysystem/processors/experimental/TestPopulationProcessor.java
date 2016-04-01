package de.vatterger.entitysystem.processors.experimental;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.application.GameConstants;
import de.vatterger.entitysystem.factory.server.TankFactory;

public class TestPopulationProcessor extends IteratingSystem {
	
	public TestPopulationProcessor() {
		super(Aspect.all());
	}
	
	@Override
	protected void initialize() {
		for (int i = 0; i < GameConstants.TANK_COUNT_INIT; i++) {
			TankFactory.createTank(world, new Vector2(MathUtils.random(0f, GameConstants.XY_BOUNDS), MathUtils.random(0f, GameConstants.XY_BOUNDS)));
		}
	}

	@Override
	protected void process(int entityId) {}
}
