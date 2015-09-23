package de.vatterger.entitysystem.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.interfaces.Modifiable;
import de.vatterger.entitysystem.interfaces.Sizeable;

public class Velocity extends Component implements Modifiable, Sizeable{
	public Vector3 vel = new Vector3(Vector3.Zero);

	public Velocity(){}

	public Velocity(Vector3 vel) {
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

	@Override
	public int getSizeInBytes() {
		return 12 + 1; //3 x float + overhead
	}
}
