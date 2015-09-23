package de.vatterger.entitysystem.components;


import com.artemis.Component;
import de.vatterger.entitysystem.interfaces.Modifiable;
import de.vatterger.entitysystem.interfaces.Sizeable;

public class Rotation extends Component implements Modifiable, Sizeable{
	public float rot;

	public Rotation() {
	}
	
	public Rotation(float rot) {
		this.rot = rot;
	}

	private boolean m = true;
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
		return 4+1; // variable float + overhead
	}
}
