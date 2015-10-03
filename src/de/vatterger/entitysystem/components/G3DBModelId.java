package de.vatterger.entitysystem.components;


import com.artemis.Component;

import de.vatterger.entitysystem.interfaces.Modifiable;
import de.vatterger.entitysystem.interfaces.Sizeable;

public class G3DBModelId extends Component implements Modifiable, Sizeable {
	/**The file-id of the G3DB Model*/
	public int id;
	
	public G3DBModelId(int id) {
		this.id = id;
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
		return 2 + 1; //variable-length-integer + overhead
	}
}
