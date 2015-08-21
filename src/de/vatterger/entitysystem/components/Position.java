package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.interfaces.Modifiable;

public class Position extends Component implements Modifiable{
	public Vector2 pos = new Vector2(Vector2.Zero);

	public Position() {
	}
	
	public Position(Vector2 pos) {
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
}
