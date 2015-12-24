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
