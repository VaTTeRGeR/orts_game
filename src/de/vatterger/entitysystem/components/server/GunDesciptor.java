package de.vatterger.entitysystem.components.server;


import com.artemis.Component;

import de.vatterger.entitysystem.interfaces.Versionable;
import de.vatterger.entitysystem.interfaces.Sizeable;

public class GunDesciptor extends Component implements Versionable, Sizeable {
	public boolean firing = false;
	public int gunTargetId = -1;

	public GunDesciptor() {
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
		return 1+4+1; // boolean + int + overhead
	}
}
