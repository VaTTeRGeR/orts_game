package de.vatterger.entitysystem.components.shared;


import com.artemis.Component;

import de.vatterger.entitysystem.interfaces.Versionable;

public class Health extends Component implements Versionable{
	/**The Health of the owning Entity */
	public float value = 0f;
	
	public Health() {
	}
	
	public Health(int value) {
		this.value = value;
	}
	
	private int v = 0;
	@Override
	public void newVersion() {
		v++;
		if(v == Integer.MAX_VALUE) {
			v = 0;
		}
	}

	@Override
	public int getVersion() {
		return v;
	}

	@Override
	public boolean compareVersion(int v2) {
		return v != v2;
	}
}
