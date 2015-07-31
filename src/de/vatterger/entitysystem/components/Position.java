package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class Position extends Component {
	public Vector3 pos = new Vector3(Vector3.Zero);

	public Position() {
	}
	
	public Position(Vector3 pos) {
		this.pos = pos;
	}
}
