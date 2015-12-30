package de.vatterger.entitysystem.components.client;


import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class LocalPosition extends Component {
	public Vector3 pos = new Vector3(Vector3.Zero);

	public LocalPosition() {
	}
	
	public LocalPosition(Vector3 pos) {
		this.pos = pos;
	}
}
