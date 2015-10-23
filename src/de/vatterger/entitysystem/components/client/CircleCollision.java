package de.vatterger.entitysystem.components.client;


import com.artemis.Component;
import com.artemis.Entity;
import de.vatterger.entitysystem.interfaces.Modifiable;
import de.vatterger.entitysystem.interfaces.Sizeable;

public class CircleCollision extends Component implements Modifiable, Sizeable {
	public float radius;
	public Entity owner;
	
	public CircleCollision() {}
	
	public CircleCollision(float radius, Entity owner) {
		this.radius = radius;
		this.owner = owner;
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
		return 4+1; // float + overhead
	}
}
