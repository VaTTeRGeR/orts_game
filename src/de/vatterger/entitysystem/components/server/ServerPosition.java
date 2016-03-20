package de.vatterger.entitysystem.components.server;


import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.interfaces.Versionable;
import de.vatterger.entitysystem.interfaces.Sizeable;

public class ServerPosition extends Component implements Versionable, Sizeable {
	public Vector3 pos = new Vector3(Vector3.Zero);

	public ServerPosition() {
	}
	
	public ServerPosition(Vector3 pos) {
		this.pos = pos;
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
	public boolean compareVersion(int v) {
		return this.v != v;
	}

	@Override
	public int getSizeInBytes() {
		return 4*3+1; // 3 x float + overhead
	}
}
