package de.vatterger.entitysystem.components.server;


import com.artemis.Component;
import de.vatterger.entitysystem.interfaces.Modifiable;
import de.vatterger.entitysystem.interfaces.Sizeable;

public class ServerRotation extends Component implements Modifiable, Sizeable {
	public float rot;

	public ServerRotation() {
	}
	
	public ServerRotation(float rot) {
		this.rot = rot;
	}

	private boolean m = true;
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

	@Override
	public int getSizeInBytes() {
		return 4+1; // float + overhead
	}
}
