package de.vatterger.entitysystem.processors.experimental;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.application.GameConstants;
import de.vatterger.entitysystem.components.server.RemoteMaster;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.factory.server.TankFactory;
import de.vatterger.entitysystem.factory.shared.EntityModifyFactory;

public class TestPopulationProcessor extends IteratingSystem {
	
	@SuppressWarnings("unchecked")
	public TestPopulationProcessor() {
		super(Aspect.all(RemoteMaster.class).exclude(Inactive.class));
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
