package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.util.EntityFactory;
import de.vatterger.entitysystem.util.GameConstants;

public class TestPopulationProcessor extends EntityProcessingSystem {

	public TestPopulationProcessor() {
		super(Aspect.getEmpty());
	}

	@Override
	protected void initialize() {
		for (int i = 0; i < GameConstants.EDIBLE_ENTITYCOUNT; i++) {
			EntityFactory.createSmallEdible(world, new Vector2(MathUtils.random(0f, GameConstants.XY_BOUNDS), MathUtils.random(0f, GameConstants.XY_BOUNDS)));
		}
		for (int i = 0; i < GameConstants.SLIME_ENTITYCOUNT; i++) {
			EntityFactory.createSlime(world, new Vector2(MathUtils.random(0f, GameConstants.XY_BOUNDS), MathUtils.random(0f, GameConstants.XY_BOUNDS)));
		}
	}

	@Override
	protected void begin() {
		for (int i = 0; i < GameConstants.EDIBLE_CREATE_PER_TICK; i++) {
			EntityFactory.createSmallEdible(world, new Vector2(MathUtils.random(0f, GameConstants.XY_BOUNDS), MathUtils.random(0f, GameConstants.XY_BOUNDS)));
		}
	}
	
	@Override
	protected void process(Entity e) {}
}
