package de.vatterger.entitysystem.processors.experimental;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.application.GameConstants;
import de.vatterger.entitysystem.factory.server.ServerTankFactory;

public class TestPopulationProcessor extends EntityProcessingSystem {
	
	public TestPopulationProcessor() {
		super(Aspect.getEmpty());
	}
	
	@Override
	protected void initialize() {
		for (int i = 0; i < GameConstants.TANK_COUNT_INIT; i++) {
			ServerTankFactory.createTank(world, new Vector2(MathUtils.random(0f, GameConstants.XY_BOUNDS), MathUtils.random(0f, GameConstants.XY_BOUNDS)));
		}
	}
	
	@Override
	protected void process(Entity e) {}
}
