package de.vatterger.entitysystem.components.server;


import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.interfaces.Modifiable;
import de.vatterger.entitysystem.interfaces.Sizeable;

public class ServerPosition extends Component implements Modifiable, Sizeable {
	public Vector3 pos = new Vector3(Vector3.Zero);

	public ServerPosition() {
	}
	
	public ServerPosition(Vector3 pos) {
		this.pos = pos;
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

	@Override
	public int getSizeInBytes() {
		return 4*3+1; // 3 x float + overhead
	}
}
