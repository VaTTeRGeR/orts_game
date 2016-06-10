package de.vatterger.techdemo.components.shared;


import com.artemis.Component;

import de.vatterger.techdemo.interfaces.Sizeable;
import de.vatterger.techdemo.interfaces.Versionable;

public class G3DBModelId extends Component implements Versionable, Sizeable {
	/**The file-id of the G3DB Model*/
	public int id;
	
	public G3DBModelId(int id) {
		this.id = id;
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
		return 2 + 1; //variable-length-integer + overhead
	}
}
