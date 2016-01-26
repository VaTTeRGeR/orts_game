package de.vatterger.entitysystem.processors.experimental;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.factory.EntityFactory;

public class TestPopulationProcessor extends EntityProcessingSystem {
	
	public TestPopulationProcessor() {
		super(Aspect.getEmpty());
	}
	
	@Override
	protected void initialize() {
		for (int i = 0; i < GameConstants.TANK_COUNT_INIT; i++) {
			EntityFactory.createTank(world, new Vector2(MathUtils.random(0f, GameConstants.XY_BOUNDS), MathUtils.random(0f, GameConstants.XY_BOUNDS)));
		}
	}
	
	@Override
	protected void process(Entity e) {}
}
