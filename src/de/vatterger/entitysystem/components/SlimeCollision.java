package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.artemis.Entity;
import com.badlogic.gdx.math.Circle;

import de.vatterger.entitysystem.interfaces.Modifiable;

public class SlimeCollision extends Component implements Modifiable{
	public Circle circle = new Circle(0, 0, 0);
	public Entity owner;
	
	public SlimeCollision() {}
	
	public SlimeCollision(float radius, Entity owner) {
		circle.setRadius(radius);
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
