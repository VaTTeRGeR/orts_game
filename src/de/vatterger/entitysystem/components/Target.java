package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.artemis.Entity;

import de.vatterger.entitysystem.interfaces.Modifiable;

public class Target extends Component implements Modifiable{
	/**The Target of the owning Entity */
	public Entity target;
	
	public Target() {
		target = null;
	}
	
	public Target(Entity target) {
		this.target = target;
	}
	
	public boolean hasTarget() {
		return target != null;
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
