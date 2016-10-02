package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class RelativePosition extends Component {
	public Vector3 position = new Vector3();

	public RelativePosition() {}
	
	public RelativePosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
	}
}
