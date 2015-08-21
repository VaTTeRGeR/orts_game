package de.vatterger.entitysystem.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.interfaces.Modifiable;

public class Velocity extends Component implements Modifiable{
	public Vector2 vel = new Vector2(Vector2.Zero);

	public Velocity(){}

	public Velocity(Vector2 vel) {
		this.vel = vel;
	}

	boolean m = true;
	@Override
	public void setIsModified() {
		m = true;
	}

	@Override
	public void setModificationApplied() {
		m = false;
	}

	@Override
	public boolean getIsModified() {
		return m;
	}
}
