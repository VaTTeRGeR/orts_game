package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class AbsolutePosition extends Component {
	public Vector3 position = new Vector3();

	public AbsolutePosition() {}
	
	public AbsolutePosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
	}
}
