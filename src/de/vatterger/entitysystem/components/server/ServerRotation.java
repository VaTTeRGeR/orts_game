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
		return 4+1; // float + overhead
	}
}
