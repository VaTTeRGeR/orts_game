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
import de.vatterger.entitysystem.util.Timer;

public class TestPopulationProcessor extends IteratingSystem {
	
	private Timer t = new Timer(0.01f);
	
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
	protected void begin() {
		t.update(world.getDelta());
	}

	@Override
	protected void process(int entityId) {
		if(t.isActive() && MathUtils.random() > 0.9f) {
			TankFactory.createTank(world, new Vector2(MathUtils.random(0f, GameConstants.XY_BOUNDS), MathUtils.random(0f, GameConstants.XY_BOUNDS))).getId();
			EntityModifyFactory.deactivateEntityOnGridmap(world.getEntity(entityId));
			t.reset();
		}
	}
}
