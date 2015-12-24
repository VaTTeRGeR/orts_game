package de.vatterger.entitysystem.components.shared;


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

	private int v = 0;
	@Override
	public void setIsModified() {
		v++;
		if(v == Integer.MAX_VALUE) {
			v = 0;
		}
	}

	@Override
	public int getModifiedVersion() {
		return v;
	}

	@Override
	public boolean getIsModified(int v2) {
		return v != v2;
	}

	@Override
	public int getSizeInBytes() {
		return 4+1; // float + overhead
	}
}
