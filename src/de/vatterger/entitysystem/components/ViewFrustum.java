package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

public class ViewFrustum extends Component {
	public Vector2 camPos = new Vector2(Vector2.Zero);

	public ViewFrustum() {
	}

	public ViewFrustum(Vector2 camPos) {
		this.camPos = camPos;
	}
}
