package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.game.components.gameobject.Position;
import de.vatterger.game.components.gameobject.Velocity;

public class MoveByVelocitySystem extends IteratingSystem {
	
	private ComponentMapper<Position>		pm;
	private ComponentMapper<Velocity>		vm;
	
	private Vector3 v0 = new Vector3();
	
	public MoveByVelocitySystem() {
		super(Aspect.all(Position.class, Velocity.class));
	}

	@Override
	protected void process(int e) {
		Vector3 pos = pm.get(e).position;
		Vector3 vel = vm.get(e).velocity;
		pos.add(v0.set(vel).scl(world.getDelta()));
	}
}
