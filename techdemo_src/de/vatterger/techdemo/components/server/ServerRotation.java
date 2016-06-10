package de.vatterger.techdemo.components.server;


import com.artemis.Component;

import de.vatterger.techdemo.interfaces.Sizeable;
import de.vatterger.techdemo.interfaces.Versionable;

public class ServerRotation extends Component implements Versionable, Sizeable {
	public float rot;

	public ServerRotation() {
	}
	
	public ServerRotation(float rot) {
		this.rot = rot;
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

	@Override
	public int getSizeInBytes() {
		return 4+1; // float + overhead
	}
}
