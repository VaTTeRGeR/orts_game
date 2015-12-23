package de.vatterger.entitysystem.components.shared;


import com.artemis.Component;

import de.vatterger.entitysystem.interfaces.Modifiable;

public class Health extends Component implements Modifiable{
	/**The Health of the owning Entity */
	public float value = 0f;
	
	public Health() {
	}
	
	public Health(int value) {
		this.value = value;
	}
	
	boolean m = true;
	@Override
	public void setIsModified() {
		m = true;
	}

	@Override
	public void resetIsModified() {
		m = false;
	}

	@Override
	public boolean getIsModified() {
		return m;
	}
}
