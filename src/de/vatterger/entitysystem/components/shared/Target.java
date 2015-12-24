package de.vatterger.entitysystem.components.shared;


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
}
