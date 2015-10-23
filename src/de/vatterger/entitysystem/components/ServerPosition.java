package de.vatterger.entitysystem.components;


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

	@Override
	public int getSizeInBytes() {
		return 3*3+1; // 3 x var float + overhead
	}
}
