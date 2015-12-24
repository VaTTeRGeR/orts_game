package de.vatterger.entitysystem.components.shared;


import com.artemis.Component;

import de.vatterger.entitysystem.interfaces.Modifiable;
import de.vatterger.entitysystem.interfaces.Sizeable;

public class G3DBModelId extends Component implements Modifiable, Sizeable {
	/**The file-id of the G3DB Model*/
	public int id;
	
	public G3DBModelId(int id) {
		this.id = id;
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
		return 2 + 1; //variable-length-integer + overhead
	}
}
