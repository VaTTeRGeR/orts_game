package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.artemis.Entity;
import de.vatterger.entitysystem.interfaces.Modifiable;

public class SlimeCollision extends Component implements Modifiable{
	public float radius;
	public Entity owner;
	
	public SlimeCollision() {}
	
	public SlimeCollision(float radius, Entity owner) {
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
}
